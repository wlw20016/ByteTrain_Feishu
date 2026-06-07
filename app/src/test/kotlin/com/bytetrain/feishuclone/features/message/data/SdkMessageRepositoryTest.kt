package com.bytetrain.feishuclone.features.message.data

import com.bytetrain.feishuclone.features.message.domain.ConversationType
import com.bytetrain.feishuclone.features.message.domain.MessageItem
import com.bytetrain.feishuclone.features.message.domain.MessagePage
import com.bytetrain.feishuclone.features.message.domain.MessageRepository
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

class SdkMessageRepositoryTest {
    @Test
    fun nativeProtobufMessageRepositoryLoadsFirstPageWithoutFallback() {
        val bridge = RecordingMessageBridge()
        val repository = SdkMessageRepository(
            sdkClient = NativeProtobufMessageSdkClient(
                bridgeClient = NativeRustFeedBridgeClient(bridge),
            ),
            fallbackRepository = null,
        )

        val page = runSuspendBlocking {
            repository.loadPage(pageSize = 20, cursor = null)
        }

        assertEquals(1, bridge.messageRequests.size)
        assertTrue(FeedProtobuf.encodePageRequest(20, null).contentEquals(bridge.messageRequests.single()))
        assertEquals(20, page.items.size)
        assertEquals("20", page.nextCursor)
        assertTrue(page.hasMore)
        assertEquals("message-1", page.items.first().id)
        assertEquals("Native Conversation 1", page.items.first().conversationName)
        assertEquals(ConversationType.GROUP, page.items.first().conversationType)
    }

    @Test
    fun nativeProtobufMessageRepositoryDelegatesBridgeErrorToFallback() {
        val fallbackRepository = RecordingMessageRepository(
            page = MessagePage(
                items = listOf(fallbackMessageItem),
                nextCursor = "fallback-next",
                hasMore = false,
            ),
        )
        val repository = SdkMessageRepository(
            sdkClient = NativeProtobufMessageSdkClient(
                bridgeClient = NativeRustFeedBridgeClient(
                    FailingMessageBridge(BridgeErrorCode.PROTOBUF_DECODE),
                ),
            ),
            fallbackRepository = fallbackRepository,
        )

        val page = runSuspendBlocking {
            repository.loadPage(pageSize = 20, cursor = "bad-cursor")
        }

        assertEquals(1, fallbackRepository.callCount)
        assertEquals("bad-cursor", fallbackRepository.lastCursor)
        assertEquals(listOf(fallbackMessageItem), page.items)
        assertEquals("fallback-next", page.nextCursor)
        assertFalse(page.hasMore)
    }

    @Test
    fun sdkBackedMessageRepositoryReturnsFirstPageWithMappedFields() {
        val repository = SdkMessageRepository(
            sdkClient = RuntimeMessageSdkClient(totalCount = 45),
            fallbackRepository = RecordingMessageRepository(),
        )

        val page = runSuspendBlocking {
            repository.loadPage(pageSize = 20, cursor = null)
        }

        assertEquals(20, page.items.size)
        assertEquals("20", page.nextCursor)
        assertTrue(page.hasMore)

        val firstItem = page.items.first()
        assertEquals("message-1", firstItem.id)
        assertEquals("Calendar Bot 1", firstItem.conversationName)
        assertEquals(ConversationType.BOT, firstItem.conversationType)
        assertEquals(null, firstItem.avatarUrl)
        assertEquals("C", firstItem.avatarText)
        assertEquals("You have a pending approval request.", firstItem.lastMessagePreview)
        assertEquals(1_717_200_000_000L, firstItem.lastMessageTimeMillis)
        assertEquals(99, firstItem.unreadCount)
        assertTrue(firstItem.isPinned)
        assertTrue(firstItem.isMuted)
        assertTrue(firstItem.isBot)
    }

    @Test
    fun sdkBackedMessageRepositoryUsesNextCursorForNextPage() {
        val repository = SdkMessageRepository(
            sdkClient = RuntimeMessageSdkClient(totalCount = 45),
            fallbackRepository = RecordingMessageRepository(),
        )

        val firstPage = runSuspendBlocking {
            repository.loadPage(pageSize = 20, cursor = null)
        }
        val nextPage = runSuspendBlocking {
            repository.loadPage(pageSize = 20, cursor = firstPage.nextCursor)
        }

        assertEquals("20", firstPage.nextCursor)
        assertEquals(20, nextPage.items.size)
        assertEquals("message-21", nextPage.items.first().id)
        assertEquals("message-40", nextPage.items.last().id)
        assertEquals("40", nextPage.nextCursor)
        assertTrue(nextPage.hasMore)
    }

    @Test
    fun sdkBackedMessageRepositoryDelegatesInvalidCursorToFallback() {
        val fallbackRepository = RecordingMessageRepository(
            page = MessagePage(
                items = listOf(fallbackMessageItem),
                nextCursor = "fallback-next",
                hasMore = false,
            ),
        )
        val repository = SdkMessageRepository(
            sdkClient = RuntimeMessageSdkClient(totalCount = 45),
            fallbackRepository = fallbackRepository,
        )

        val page = runSuspendBlocking {
            repository.loadPage(pageSize = 20, cursor = "not-a-number")
        }

        assertEquals(1, fallbackRepository.callCount)
        assertEquals(20, fallbackRepository.lastPageSize)
        assertEquals("not-a-number", fallbackRepository.lastCursor)
        assertEquals(listOf(fallbackMessageItem), page.items)
        assertEquals("fallback-next", page.nextCursor)
        assertFalse(page.hasMore)
    }

    private class RecordingMessageRepository(
        private val page: MessagePage = MessagePage(
            items = emptyList(),
            nextCursor = null,
            hasMore = false,
        ),
    ) : MessageRepository {
        var callCount: Int = 0
            private set
        var lastPageSize: Int? = null
            private set
        var lastCursor: String? = null
            private set

        override suspend fun loadPage(pageSize: Int, cursor: String?): MessagePage {
            callCount += 1
            lastPageSize = pageSize
            lastCursor = cursor
            return page
        }
    }
}

private val fallbackMessageItem = MessageItem(
    id = "fallback-message",
    conversationName = "Fallback Conversation",
    conversationType = ConversationType.SINGLE,
    avatarUrl = null,
    avatarText = "F",
    lastMessagePreview = "Fallback message",
    lastMessageTimeMillis = 1L,
    unreadCount = 0,
    isPinned = false,
    isMuted = false,
    isBot = false,
)

private class RecordingMessageBridge : RustFeedBridge {
    val messageRequests = mutableListOf<ByteArray>()

    override fun readMessagePage(requestBytes: ByteArray): ByteArray {
        messageRequests += requestBytes
        val request = decodePageRequest(requestBytes)
        return encodeMessagePageResponse(request.pageSize, request.cursor)
    }

    override fun readMailPage(requestBytes: ByteArray): ByteArray =
        throw SdkBridgeException("unexpected mail bridge call", BridgeErrorCode.NATIVE_BRIDGE)
}

private class FailingMessageBridge(
    private val code: BridgeErrorCode,
) : RustFeedBridge {
    override fun readMessagePage(requestBytes: ByteArray): ByteArray =
        throw SdkBridgeException("simulated native bridge error", code)

    override fun readMailPage(requestBytes: ByteArray): ByteArray =
        throw SdkBridgeException("unexpected mail bridge call", BridgeErrorCode.NATIVE_BRIDGE)
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

private fun encodeMessagePageResponse(pageSize: Int, cursor: String?): ByteArray {
    val startIndex = cursor?.toIntOrNull() ?: 0
    val output = ByteArrayOutputStream()
    repeat(pageSize) { offset ->
        val index = startIndex + offset
        writeBytesField(output, 1, encodeMessageItem(index))
    }
    writeBytesField(output, 2, encodePageInfo("${startIndex + pageSize}", hasMore = true))
    return output.toByteArray()
}

private fun encodeMessageItem(index: Int): ByteArray {
    val output = ByteArrayOutputStream()
    writeStringField(output, 1, "message-${index + 1}")
    writeStringField(output, 2, "Native Conversation ${index + 1}")
    writeInt32Field(output, 3, 2)
    writeStringField(output, 5, "N")
    writeStringField(output, 6, "Native protobuf preview ${index + 1}")
    writeInt64Field(output, 7, 1_717_200_000_000L - index * 60_000L)
    writeInt32Field(output, 8, index % 5)
    writeBoolField(output, 9, index == 0)
    writeBoolField(output, 10, false)
    writeBoolField(output, 11, false)
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
