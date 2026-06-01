package com.bytetrain.feishuclone.features.message.data

import com.bytetrain.feishuclone.features.message.domain.ConversationType
import com.bytetrain.feishuclone.features.message.domain.MessageItem
import com.bytetrain.feishuclone.features.message.domain.MessagePage
import com.bytetrain.feishuclone.features.message.domain.MessageRepository

class MockMessageRepository(
    private val totalCount: Int = DEFAULT_TOTAL_COUNT,
    private val baseTimeMillis: Long = DEFAULT_BASE_TIME_MILLIS,
) : MessageRepository {
    private val items: List<MessageItem> by lazy {
        List(totalCount) { index -> createMessage(index) }
    }

    override suspend fun loadPage(pageSize: Int, cursor: String?): MessagePage {
        if (pageSize <= 0 || totalCount <= 0) {
            return MessagePage(items = emptyList(), nextCursor = null, hasMore = false)
        }

        val startIndex = cursor?.toIntOrNull()?.coerceIn(0, totalCount) ?: 0
        val endIndex = (startIndex + pageSize).coerceAtMost(totalCount)
        val pageItems = items.subList(startIndex, endIndex)
        val hasMore = endIndex < totalCount

        return MessagePage(
            items = pageItems,
            nextCursor = if (hasMore) endIndex.toString() else null,
            hasMore = hasMore,
        )
    }

    private fun createMessage(index: Int): MessageItem {
        val type = conversationTypeFor(index)
        val name = conversationNameFor(index, type)

        return MessageItem(
            id = "message-${index + 1}",
            conversationName = name,
            conversationType = type,
            avatarUrl = null,
            avatarText = avatarTextFor(name),
            lastMessagePreview = lastMessagePreviewFor(index, type),
            lastMessageTimeMillis = baseTimeMillis - index * MESSAGE_INTERVAL_MILLIS,
            unreadCount = unreadCountFor(index),
            isPinned = index % 17 == 0,
            isMuted = index % 11 == 0,
            isBot = type == ConversationType.BOT,
        )
    }

    private fun conversationTypeFor(index: Int): ConversationType =
        when {
            index % 9 == 0 -> ConversationType.BOT
            index % 3 == 0 -> ConversationType.GROUP
            else -> ConversationType.SINGLE
        }

    private fun conversationNameFor(index: Int, type: ConversationType): String =
        when (type) {
            ConversationType.SINGLE -> "${singleNames[index % singleNames.size]} ${index + 1}"
            ConversationType.GROUP -> "${groupNames[index % groupNames.size]} ${index + 1}"
            ConversationType.BOT -> "${botNames[index % botNames.size]} ${index + 1}"
        }

    private fun avatarTextFor(name: String): String =
        name.firstOrNull()?.uppercaseChar()?.toString() ?: "M"

    private fun lastMessagePreviewFor(index: Int, type: ConversationType): String =
        when (type) {
            ConversationType.SINGLE -> singlePreviews[index % singlePreviews.size]
            ConversationType.GROUP -> groupPreviews[index % groupPreviews.size]
            ConversationType.BOT -> botPreviews[index % botPreviews.size]
        }

    private fun unreadCountFor(index: Int): Int =
        when {
            index % 13 == 0 -> 99
            index % 4 == 0 -> index % 12 + 1
            else -> 0
        }

    companion object {
        const val DEFAULT_TOTAL_COUNT = 10_000
        private const val DEFAULT_BASE_TIME_MILLIS = 1_717_200_000_000L
        private const val MESSAGE_INTERVAL_MILLIS = 60_000L

        private val singleNames = listOf(
            "Alex Chen",
            "Mia Zhang",
            "Noah Liu",
            "Emma Wang",
            "Kai Huang",
            "Nina Zhao",
        )

        private val groupNames = listOf(
            "Product Squad",
            "Android Guild",
            "Release Room",
            "Design Review",
            "Training Camp",
        )

        private val botNames = listOf(
            "Calendar Bot",
            "Approval Bot",
            "Build Bot",
            "Docs Bot",
        )

        private val singlePreviews = listOf(
            "Can you review the latest draft?",
            "I pushed the small fix we discussed.",
            "Let's sync after the standup.",
            "The new mock data looks good to me.",
        )

        private val groupPreviews = listOf(
            "Meeting notes are ready for review.",
            "Please update your progress before 6 PM.",
            "The first page flow is ready for smoke testing.",
            "We still need mapper coverage for the shared model.",
        )

        private val botPreviews = listOf(
            "You have a pending approval request.",
            "Daily build completed successfully.",
            "A calendar event starts in 10 minutes.",
            "Documentation reminder: add acceptance evidence.",
        )
    }
}
