package com.bytetrain.feishuclone.features.mail.data

import com.bytetrain.feishuclone.features.mail.domain.MailItem
import com.bytetrain.feishuclone.features.mail.domain.MailPage
import com.bytetrain.feishuclone.features.mail.domain.MailRepository
import com.bytetrain.feishuclone.features.mail.domain.MailType
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
