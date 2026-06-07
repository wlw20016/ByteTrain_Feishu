package com.bytetrain.feishuclone.features.mail.data

import com.bytetrain.feishuclone.features.mail.domain.MailItem
import com.bytetrain.feishuclone.features.mail.domain.MailPage
import com.bytetrain.feishuclone.features.mail.domain.MailRepository
import com.bytetrain.feishuclone.features.mail.domain.MailType
import com.bytetrain.feishuclone.sdk.BridgeErrorCode
import com.bytetrain.feishuclone.sdk.FeedProtobuf
import com.bytetrain.feishuclone.sdk.NativeRustFeedBridgeClient
import com.bytetrain.feishuclone.sdk.ProtoDecoder
import com.bytetrain.feishuclone.sdk.RustFeedBridge
import com.bytetrain.feishuclone.sdk.SdkBridgeException
import com.bytetrain.feishuclone.sdk.WIRE_LEN
import com.bytetrain.feishuclone.sdk.WIRE_VARINT
import java.io.ByteArrayOutputStream
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SdkMailRepositoryTest {
    @Test
    fun nativeProtobufMailRepositoryLoadsFirstPageWithoutFallback() {
        val bridge = RecordingMailBridge()
        val repository = SdkMailRepository(
            sdkClient = NativeProtobufMailSdkClient(
                bridgeClient = NativeRustFeedBridgeClient(bridge),
            ),
            fallbackRepository = null,
        )

        val page = runSuspendBlocking {
            repository.loadPage(pageSize = 20, cursor = null)
        }

        assertEquals(1, bridge.mailRequests.size)
        assertTrue(FeedProtobuf.encodePageRequest(20, null).contentEquals(bridge.mailRequests.single()))
        assertEquals(20, page.items.size)
        assertEquals("20", page.nextCursor)
        assertTrue(page.hasMore)
        assertEquals("mail-1", page.items.first().id)
        assertEquals("Native Sender 1", page.items.first().sender)
        assertEquals(MailType.REMINDER, page.items.first().mailType)
    }

    @Test
    fun nativeProtobufMailRepositoryDelegatesBridgeErrorToFallback() {
        val fallbackRepository = RecordingMailRepository(
            page = MailPage(
                items = listOf(fallbackMailItem),
                nextCursor = "fallback-next",
                hasMore = false,
            ),
        )
        val repository = SdkMailRepository(
            sdkClient = NativeProtobufMailSdkClient(
                bridgeClient = NativeRustFeedBridgeClient(
                    FailingMailBridge(BridgeErrorCode.INVALID_CURSOR),
                ),
            ),
            fallbackRepository = fallbackRepository,
        )

        val page = runSuspendBlocking {
            repository.loadPage(pageSize = 20, cursor = "bad-cursor")
        }

        assertEquals(1, fallbackRepository.callCount)
        assertEquals("bad-cursor", fallbackRepository.lastCursor)
        assertEquals(listOf(fallbackMailItem), page.items)
        assertEquals("fallback-next", page.nextCursor)
        assertFalse(page.hasMore)
    }

    @Test
    fun sdkBackedMailRepositoryReturnsFirstPageWithMappedFields() {
        val repository = SdkMailRepository(
            sdkClient = RuntimeMailSdkClient(totalCount = 45),
            fallbackRepository = RecordingMailRepository(),
        )

        val page = runSuspendBlocking {
            repository.loadPage(pageSize = 20, cursor = null)
        }

        assertEquals(20, page.items.size)
        assertEquals("20", page.nextCursor)
        assertTrue(page.hasMore)

        val firstItem = page.items.first()
        assertEquals("mail-1", firstItem.id)
        assertEquals("Feishu Updates", firstItem.sender)
        assertEquals("Weekly product digest #1", firstItem.subject)
        assertEquals("Here are the highlights and decisions from this week.", firstItem.preview)
        assertEquals(1_717_200_000_000L, firstItem.timestampMillis)
        assertTrue(firstItem.unread)
        assertEquals(0, firstItem.attachmentCount)
        assertEquals(MailType.UPDATE, firstItem.mailType)
        assertEquals(null, firstItem.actionText)
    }

    @Test
    fun sdkBackedMailRepositoryUsesNextCursorForNextPage() {
        val repository = SdkMailRepository(
            sdkClient = RuntimeMailSdkClient(totalCount = 45),
            fallbackRepository = RecordingMailRepository(),
        )

        val firstPage = runSuspendBlocking {
            repository.loadPage(pageSize = 20, cursor = null)
        }
        val nextPage = runSuspendBlocking {
            repository.loadPage(pageSize = 20, cursor = firstPage.nextCursor)
        }

        assertEquals("20", firstPage.nextCursor)
        assertEquals(20, nextPage.items.size)
        assertEquals("mail-21", nextPage.items.first().id)
        assertEquals("mail-40", nextPage.items.last().id)
        assertEquals("40", nextPage.nextCursor)
        assertTrue(nextPage.hasMore)
    }

    @Test
    fun sdkBackedMailRepositoryDelegatesInvalidCursorToFallback() {
        val fallbackRepository = RecordingMailRepository(
            page = MailPage(
                items = listOf(fallbackMailItem),
                nextCursor = "fallback-next",
                hasMore = false,
            ),
        )
        val repository = SdkMailRepository(
            sdkClient = RuntimeMailSdkClient(totalCount = 45),
            fallbackRepository = fallbackRepository,
        )

        val page = runSuspendBlocking {
            repository.loadPage(pageSize = 20, cursor = "not-a-number")
        }

        assertEquals(1, fallbackRepository.callCount)
        assertEquals(20, fallbackRepository.lastPageSize)
        assertEquals("not-a-number", fallbackRepository.lastCursor)
        assertEquals(listOf(fallbackMailItem), page.items)
        assertEquals("fallback-next", page.nextCursor)
        assertFalse(page.hasMore)
    }

    private class RecordingMailRepository(
        private val page: MailPage = MailPage(
            items = emptyList(),
            nextCursor = null,
            hasMore = false,
        ),
    ) : MailRepository {
        var callCount: Int = 0
            private set
        var lastPageSize: Int? = null
            private set
        var lastCursor: String? = null
            private set

        override suspend fun loadPage(pageSize: Int, cursor: String?): MailPage {
            callCount += 1
            lastPageSize = pageSize
            lastCursor = cursor
            return page
        }
    }
}

private val fallbackMailItem = MailItem(
    id = "fallback-mail",
    sender = "Fallback Sender",
    subject = "Fallback Subject",
    preview = "Fallback preview",
    timestampMillis = 1L,
    unread = false,
    attachmentCount = 1,
    mailType = MailType.REPORT,
    actionText = "Open report",
)

private class RecordingMailBridge : RustFeedBridge {
    val mailRequests = mutableListOf<ByteArray>()

    override fun readMessagePage(requestBytes: ByteArray): ByteArray =
        throw SdkBridgeException("unexpected message bridge call", BridgeErrorCode.NATIVE_BRIDGE)

    override fun readMailPage(requestBytes: ByteArray): ByteArray {
        mailRequests += requestBytes
        val request = decodePageRequest(requestBytes)
        return encodeMailPageResponse(request.pageSize, request.cursor)
    }
}

private class FailingMailBridge(
    private val code: BridgeErrorCode,
) : RustFeedBridge {
    override fun readMessagePage(requestBytes: ByteArray): ByteArray =
        throw SdkBridgeException("unexpected message bridge call", BridgeErrorCode.NATIVE_BRIDGE)

    override fun readMailPage(requestBytes: ByteArray): ByteArray =
        throw SdkBridgeException("simulated native bridge error", code)
}

private data class TestPageRequest(
    val pageSize: Int,
    val cursor: String?,
)

private fun decodePageRequest(bytes: ByteArray): TestPageRequest {
    var pageSize = 0
    var cursor: String? = null
    val decoder = ProtoDecoder(bytes)

    while (true) {
        val field = decoder.nextField() ?: break
        when (field.number) {
            1 -> {
                field.expectWireType(WIRE_VARINT)
                pageSize = field.readInt32()
            }
            2 -> {
                field.expectWireType(WIRE_LEN)
                cursor = field.readString()
            }
            else -> field.skip()
        }
    }

    return TestPageRequest(pageSize, cursor)
}

private fun encodeMailPageResponse(pageSize: Int, cursor: String?): ByteArray {
    val startIndex = cursor?.toIntOrNull() ?: 0
    val output = ByteArrayOutputStream()
    repeat(pageSize) { offset ->
        val index = startIndex + offset
        writeBytesField(output, 1, encodeMailItem(index))
    }
    writeBytesField(output, 2, encodePageInfo("${startIndex + pageSize}", hasMore = true))
    return output.toByteArray()
}

private fun encodeMailItem(index: Int): ByteArray {
    val output = ByteArrayOutputStream()
    writeStringField(output, 1, "mail-${index + 1}")
    writeStringField(output, 2, "Native Sender ${index + 1}")
    writeStringField(output, 3, "Native protobuf subject ${index + 1}")
    writeStringField(output, 4, "Native protobuf preview ${index + 1}")
    writeInt64Field(output, 5, 1_717_200_000_000L - index * 300_000L)
    writeBoolField(output, 6, index == 0)
    writeInt32Field(output, 7, index % 3)
    writeInt32Field(output, 8, 1)
    writeStringField(output, 9, "View")
    return output.toByteArray()
}

private fun encodePageInfo(nextCursor: String, hasMore: Boolean): ByteArray {
    val output = ByteArrayOutputStream()
    writeStringField(output, 1, nextCursor)
    writeBoolField(output, 2, hasMore)
    return output.toByteArray()
}

private fun writeStringField(output: ByteArrayOutputStream, number: Int, value: String) {
    writeBytesField(output, number, value.encodeToByteArray())
}

private fun writeBytesField(output: ByteArrayOutputStream, number: Int, value: ByteArray) {
    writeKey(output, number, WIRE_LEN)
    writeVarint(output, value.size.toLong())
    output.write(value)
}

private fun writeInt32Field(output: ByteArrayOutputStream, number: Int, value: Int) {
    writeKey(output, number, WIRE_VARINT)
    writeVarint(output, value.toLong())
}

private fun writeInt64Field(output: ByteArrayOutputStream, number: Int, value: Long) {
    writeKey(output, number, WIRE_VARINT)
    writeVarint(output, value)
}

private fun writeBoolField(output: ByteArrayOutputStream, number: Int, value: Boolean) {
    writeKey(output, number, WIRE_VARINT)
    writeVarint(output, if (value) 1L else 0L)
}

private fun writeKey(output: ByteArrayOutputStream, number: Int, wireType: Int) {
    writeVarint(output, (number.toLong() shl 3) or wireType.toLong())
}

private fun writeVarint(output: ByteArrayOutputStream, value: Long) {
    var remaining = value
    while (remaining >= 0x80) {
        output.write(((remaining and 0x7f) or 0x80).toInt())
        remaining = remaining ushr 7
    }
    output.write(remaining.toInt())
}

private fun <T> runSuspendBlocking(block: suspend () -> T): T {
    var value: T? = null
    var error: Throwable? = null

    block.startCoroutine(object : Continuation<T> {
        override val context = EmptyCoroutineContext

        override fun resumeWith(result: Result<T>) {
            result.fold(
                onSuccess = { value = it },
                onFailure = { error = it },
            )
        }
    })

    error?.let { throw it }
    return assertNotNull(value)
}
