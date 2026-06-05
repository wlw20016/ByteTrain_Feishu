package com.bytetrain.feishuclone.features.mail.data

import com.bytetrain.feishuclone.features.mail.domain.MailType

class RuntimeMailSdkClient(
    totalCount: Int = DEFAULT_TOTAL_COUNT,
    private val baseTimeMillis: Long = DEFAULT_BASE_TIME_MILLIS,
) : MailSdkClient {
    private val items: List<SdkMailItem>

    init {
        require(totalCount >= 0) { "totalCount must be >= 0" }
        items = List(totalCount) { index -> createMail(index) }
    }

    override suspend fun getMailPage(pageSize: Int, cursor: String?): SdkMailPage {
        val page = paginate(items, pageSize, cursor)

        return SdkMailPage(
            items = page.items,
            nextCursor = page.nextCursor,
            hasMore = page.hasMore,
        )
    }

    private fun createMail(index: Int): SdkMailItem =
        SdkMailItem(
            id = "mail-${index + 1}",
            sender = senders[index % senders.size],
            subject = "${subjects[index % subjects.size]} #${index + 1}",
            preview = previews[index % previews.size],
            timestampMillis = baseTimeMillis - index * MAIL_INTERVAL_MILLIS,
            unread = index % 3 == 0,
            attachmentCount = 0,
            mailType = MailType.UPDATE,
            actionText = null,
        )

    companion object {
        const val DEFAULT_TOTAL_COUNT = 10_000
        private const val DEFAULT_BASE_TIME_MILLIS = 1_717_200_000_000L
        private const val MAIL_INTERVAL_MILLIS = 300_000L

        private val senders = listOf(
            "Feishu Updates",
            "Product Ops",
            "Design Team",
            "QA Desk",
            "Build System",
            "Learning Center",
        )

        private val subjects = listOf(
            "Weekly product digest",
            "Action required for release",
            "Design review notes",
            "Regression test summary",
            "Build pipeline report",
            "Training reminder",
        )

        private val previews = listOf(
            "Here are the highlights and decisions from this week.",
            "Please confirm owners before the release window closes.",
            "The annotated mockups are ready for implementation review.",
            "Smoke testing passed with a few follow-up checks remaining.",
            "Nightly build finished and artifacts are available.",
            "Your assigned course is due later this week.",
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
