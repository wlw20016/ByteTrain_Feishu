package com.bytetrain.feishuclone.features.message.data

import com.bytetrain.feishuclone.features.message.domain.ConversationType

class RuntimeMessageSdkClient(
    totalCount: Int = DEFAULT_TOTAL_COUNT,
    private val baseTimeMillis: Long = DEFAULT_BASE_TIME_MILLIS,
) : MessageSdkClient {
    private val items: List<SdkMessageItem>

    init {
        require(totalCount >= 0) { "totalCount must be >= 0" }
        items = List(totalCount) { index -> createMessage(index) }
    }

    override suspend fun getMessagePage(pageSize: Int, cursor: String?): SdkMessagePage {
        val page = paginate(items, pageSize, cursor)

        return SdkMessagePage(
            items = page.items,
            nextCursor = page.nextCursor,
            hasMore = page.hasMore,
        )
    }

    private fun createMessage(index: Int): SdkMessageItem {
        val conversationType = conversationTypeFor(index)
        val conversationName = conversationNameFor(index, conversationType)

        return SdkMessageItem(
            id = "message-${index + 1}",
            conversationName = conversationName,
            conversationType = conversationType,
            avatarUrl = null,
            avatarText = avatarTextFor(conversationName),
            lastMessagePreview = lastMessagePreviewFor(index, conversationType),
            lastMessageTimeMillis = baseTimeMillis - index * MESSAGE_INTERVAL_MILLIS,
            unreadCount = unreadCountFor(index),
            isPinned = index % 17 == 0,
            isMuted = index % 11 == 0,
            isBot = conversationType == ConversationType.BOT,
        )
    }

    private fun conversationTypeFor(index: Int): ConversationType =
        when {
            index % 9 == 0 -> ConversationType.BOT
            index % 3 == 0 -> ConversationType.GROUP
            else -> ConversationType.SINGLE
        }

    private fun conversationNameFor(index: Int, conversationType: ConversationType): String {
        val names = when (conversationType) {
            ConversationType.SINGLE -> singleNames
            ConversationType.GROUP -> groupNames
            ConversationType.BOT -> botNames
        }

        return "${names[index % names.size]} ${index + 1}"
    }

    private fun avatarTextFor(name: String): String =
        name.firstOrNull()?.uppercaseChar()?.toString() ?: "M"

    private fun lastMessagePreviewFor(index: Int, conversationType: ConversationType): String =
        when (conversationType) {
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

private data class RuntimePage<T>(
    val items: List<T>,
    val nextCursor: String?,
    val hasMore: Boolean,
)

private fun <T> paginate(items: List<T>, pageSize: Int, cursor: String?): RuntimePage<T> {
    validatePageSize(pageSize)

    val startIndex = parseCursor(cursor, items.size)
    val endIndex = (startIndex + pageSize).coerceAtMost(items.size)
    val hasMore = endIndex < items.size

    return RuntimePage(
        items = items.subList(startIndex, endIndex),
        nextCursor = if (hasMore) endIndex.toString() else null,
        hasMore = hasMore,
    )
}

private fun validatePageSize(pageSize: Int) {
    if (pageSize !in MIN_PAGE_SIZE..MAX_PAGE_SIZE) {
        throw SdkBridgeException("invalid page_size $pageSize; expected value in $MIN_PAGE_SIZE..=$MAX_PAGE_SIZE")
    }
}

private fun parseCursor(cursor: String?, totalCount: Int): Int =
    when {
        cursor.isNullOrEmpty() -> 0
        else -> {
            val startIndex = cursor.toIntOrNull()
            if (startIndex == null || startIndex < 0) {
                throw SdkBridgeException("invalid cursor '$cursor'")
            }
            if (startIndex > totalCount) {
                throw SdkBridgeException("cursor '$cursor' is outside the item range 0..=$totalCount")
            }
            startIndex
        }
    }

private class SdkBridgeException(message: String) : IllegalArgumentException(message)

private const val MIN_PAGE_SIZE = 1
private const val MAX_PAGE_SIZE = 200
