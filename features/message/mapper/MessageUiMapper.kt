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
        badges += BadgeModel(text = "置顶", tone = "pinned")
    }
    if (isMuted) {
        badges += BadgeModel(text = "免打扰", tone = "muted")
    }
    if (isBot) {
        badges += BadgeModel(text = "机器人", tone = "bot")
    }

    return badges
}

private fun MessageItem.toDetailModel(): DetailModel =
    DetailModel(
        id = id,
        title = conversationName,
        body = lastMessagePreview,
        metas = listOf(
            DetailMeta(label = "类型", value = conversationType.displayText()),
            DetailMeta(label = "未读", value = unreadCount.toString()),
            DetailMeta(label = "置顶", value = isPinned.yesNoText()),
            DetailMeta(label = "免打扰", value = isMuted.yesNoText()),
            DetailMeta(label = "机器人", value = isBot.yesNoText()),
            DetailMeta(label = "时间", value = formatMessageTimestamp(lastMessageTimeMillis)),
        ),
    )

private fun ConversationType.displayText(): String =
    when (this) {
        ConversationType.SINGLE -> "单聊"
        ConversationType.GROUP -> "群聊"
        ConversationType.BOT -> "机器人"
    }

private fun ConversationType.avatarBackgroundColor(): String =
    when (this) {
        ConversationType.SINGLE -> "#2F80ED"
        ConversationType.GROUP -> "#27AE60"
        ConversationType.BOT -> "#9B51E0"
    }

private fun Boolean.yesNoText(): String =
    if (this) "是" else "否"

private fun String.firstLabel(): String =
    firstOrNull()?.uppercaseChar()?.toString() ?: "M"

private fun formatMessageTimestamp(timestampMillis: Long): String =
    requireNotNull(timestampFormatter.get()).format(Date(timestampMillis))

private val timestampFormatter = ThreadLocal.withInitial {
    SimpleDateFormat("HH:mm", Locale.US)
}

private const val MAX_VISIBLE_UNREAD = 99
