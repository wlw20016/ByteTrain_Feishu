package com.bytetrain.feishuclone.features.message.data

import com.bytetrain.feishuclone.features.message.domain.ConversationType
import com.bytetrain.feishuclone.features.message.domain.MessageItem
import com.bytetrain.feishuclone.features.message.domain.MessagePage
import com.bytetrain.feishuclone.features.message.domain.MessageRepository
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
