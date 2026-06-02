package com.bytetrain.feishuclone.features.message.mapper

import com.bytetrain.feishuclone.features.message.domain.ConversationType
import com.bytetrain.feishuclone.features.message.domain.MessageItem
import com.bytetrain.feishuclone.shared.ui.AvatarModel
import com.bytetrain.feishuclone.shared.ui.BadgeModel
import com.bytetrain.feishuclone.shared.ui.DetailMeta
import com.bytetrain.feishuclone.shared.ui.DetailModel
import com.bytetrain.feishuclone.shared.ui.DisplayStyle
import com.bytetrain.feishuclone.shared.ui.UnifiedListItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun MessageItem.toUnifiedListItem(): UnifiedListItem =
    UnifiedListItem(
        id = id,
        title = conversationName,
        subtitle = lastMessagePreview,
        timestampText = formatMessageTimestamp(lastMessageTimeMillis),
        avatar = AvatarModel(
            label = avatarText.ifBlank { conversationName.firstLabel() },
            imageUrl = avatarUrl,
            backgroundColor = conversationType.avatarBackgroundColor(),
        ),
        badges = toBadges(),
        displayStyle = DisplayStyle.DENSE_CONVERSATION,
        detail = toDetailModel(),
    )

private fun MessageItem.toBadges(): List<BadgeModel> {
    val badges = mutableListOf<BadgeModel>()

    if (unreadCount > 0) {
        badges += BadgeModel(
            text = if (unreadCount > MAX_VISIBLE_UNREAD) "$MAX_VISIBLE_UNREAD+" else unreadCount.toString(),
            tone = "unread",
        )
    }
    if (isPinned) {
        badges += BadgeModel(text = "Pinned", tone = "pinned")
    }
    if (isMuted) {
        badges += BadgeModel(text = "Muted", tone = "muted")
    }
    if (isBot) {
        badges += BadgeModel(text = "Bot", tone = "bot")
    }

    return badges
}

private fun MessageItem.toDetailModel(): DetailModel =
    DetailModel(
        id = id,
        title = conversationName,
        body = lastMessagePreview,
        metas = listOf(
            DetailMeta(label = "Type", value = conversationType.displayText()),
            DetailMeta(label = "Unread", value = unreadCount.toString()),
            DetailMeta(label = "Pinned", value = isPinned.yesNoText()),
            DetailMeta(label = "Muted", value = isMuted.yesNoText()),
            DetailMeta(label = "Bot", value = isBot.yesNoText()),
            DetailMeta(label = "Time", value = formatMessageTimestamp(lastMessageTimeMillis)),
        ),
    )

private fun ConversationType.displayText(): String =
    when (this) {
        ConversationType.SINGLE -> "Single"
        ConversationType.GROUP -> "Group"
        ConversationType.BOT -> "Bot"
    }

private fun ConversationType.avatarBackgroundColor(): String =
    when (this) {
        ConversationType.SINGLE -> "#2F80ED"
        ConversationType.GROUP -> "#27AE60"
        ConversationType.BOT -> "#9B51E0"
    }

private fun Boolean.yesNoText(): String =
    if (this) "Yes" else "No"

private fun String.firstLabel(): String =
    firstOrNull()?.uppercaseChar()?.toString() ?: "M"

private fun formatMessageTimestamp(timestampMillis: Long): String =
    requireNotNull(timestampFormatter.get()).format(Date(timestampMillis))

private val timestampFormatter = ThreadLocal.withInitial {
    SimpleDateFormat("HH:mm", Locale.US)
}

private const val MAX_VISIBLE_UNREAD = 99
