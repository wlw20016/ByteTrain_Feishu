package com.bytetrain.feishuclone.features.mail.data

import com.bytetrain.feishuclone.features.mail.domain.MailItem
import com.bytetrain.feishuclone.features.mail.domain.MailPage
import com.bytetrain.feishuclone.features.mail.domain.MailRepository
import com.bytetrain.feishuclone.features.mail.domain.MailType

class MockMailRepository(
    private val totalCount: Int = DEFAULT_TOTAL_COUNT,
    private val baseTimeMillis: Long = DEFAULT_BASE_TIME_MILLIS,
) : MailRepository {
    private val items: List<MailItem> by lazy {
        List(totalCount) { index -> createMail(index) }
    }

    override suspend fun loadPage(pageSize: Int, cursor: String?): MailPage {
        if (pageSize <= 0 || totalCount <= 0) {
            return MailPage(items = emptyList(), nextCursor = null, hasMore = false)
        }

        val startIndex = cursor?.toIntOrNull()?.coerceIn(0, totalCount) ?: 0
        val endIndex = (startIndex + pageSize).coerceAtMost(totalCount)
        val pageItems = items.subList(startIndex, endIndex)
        val hasMore = endIndex < totalCount

        return MailPage(
            items = pageItems,
            nextCursor = if (hasMore) endIndex.toString() else null,
            hasMore = hasMore,
        )
    }

    private fun createMail(index: Int): MailItem {
        val mailType = mailTypeFor(index)

        return MailItem(
            id = "mail-${index + 1}",
            sender = senders[index % senders.size],
            subject = "${subjects[index % subjects.size]} #${index + 1}",
            preview = previews[index % previews.size],
            timestampMillis = baseTimeMillis - index * MAIL_INTERVAL_MILLIS,
            unread = index % 3 == 0,
            attachmentCount = attachmentCountFor(index),
            mailType = mailType,
            actionText = actionTextFor(index, mailType),
        )
    }

    private fun mailTypeFor(index: Int): MailType =
        when {
            index % 11 == 0 -> MailType.SYSTEM
            index % 7 == 0 -> MailType.REPORT
            index % 5 == 0 -> MailType.COLLABORATION
            index % 3 == 0 -> MailType.REMINDER
            else -> MailType.UPDATE
        }

    private fun attachmentCountFor(index: Int): Int =
        when {
            index % 8 == 0 -> 3
            index % 5 == 0 -> 2
            index % 2 == 0 -> 1
            else -> 0
        }

    private fun actionTextFor(index: Int, mailType: MailType): String? =
        when {
            index % 4 != 0 -> null
            mailType == MailType.REMINDER -> "Confirm"
            mailType == MailType.REPORT -> "Open report"
            mailType == MailType.SYSTEM -> "View build"
            mailType == MailType.COLLABORATION -> "Review"
            else -> "Read"
        }

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
            "The latest interaction notes are ready for your pass.",
            "Smoke coverage passed for the message main flow.",
            "The latest debug build artifact is available.",
            "A short lesson is waiting in the learning center.",
        )
    }
}
