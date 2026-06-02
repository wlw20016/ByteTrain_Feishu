package com.bytetrain.feishuclone.features.mail.mapper

import com.bytetrain.feishuclone.features.mail.domain.MailItem
import com.bytetrain.feishuclone.features.mail.domain.MailType
import com.bytetrain.feishuclone.shared.ui.AvatarModel
import com.bytetrain.feishuclone.shared.ui.BadgeModel
import com.bytetrain.feishuclone.shared.ui.DetailMeta
import com.bytetrain.feishuclone.shared.ui.DetailModel
import com.bytetrain.feishuclone.shared.ui.DisplayStyle
import com.bytetrain.feishuclone.shared.ui.UnifiedListItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun MailItem.toUnifiedListItem(): UnifiedListItem =
    UnifiedListItem(
        id = id,
        title = subject,
        subtitle = preview,
        timestampText = formatMailTimestamp(timestampMillis),
        avatar = AvatarModel(
            label = sender.firstLabel(),
            imageUrl = null,
            backgroundColor = mailType.avatarBackgroundColor(),
        ),
        badges = toBadges(),
        displayStyle = DisplayStyle.MAIL_CARD,
        detail = toDetailModel(),
    )

private fun MailItem.toBadges(): List<BadgeModel> {
    val badges = mutableListOf<BadgeModel>()

    if (unread) {
        badges += BadgeModel(text = "Unread", tone = "unread")
    }
    if (attachmentCount > 0) {
        badges += BadgeModel(text = "$attachmentCount attachments", tone = "attachment")
    }
    badges += BadgeModel(text = mailType.displayText(), tone = mailType.badgeTone())
    actionText?.takeIf { it.isNotBlank() }?.let { text ->
        badges += BadgeModel(text = text, tone = "action")
    }

    return badges
}

private fun MailItem.toDetailModel(): DetailModel =
    DetailModel(
        id = id,
        title = subject,
        body = preview,
        metas = listOfNotNull(
            DetailMeta(label = "Sender", value = sender),
            DetailMeta(label = "Type", value = mailType.displayText()),
            DetailMeta(label = "Unread", value = unread.yesNoText()),
            DetailMeta(label = "Attachments", value = attachmentCount.toString()),
            actionText?.takeIf { it.isNotBlank() }?.let { DetailMeta(label = "Action", value = it) },
            DetailMeta(label = "Time", value = formatMailTimestamp(timestampMillis)),
        ),
    )

private fun MailType.displayText(): String =
    when (this) {
        MailType.REMINDER -> "Reminder"
        MailType.SYSTEM -> "System"
        MailType.COLLABORATION -> "Collaboration"
        MailType.REPORT -> "Report"
        MailType.UPDATE -> "Update"
    }

private fun MailType.badgeTone(): String =
    when (this) {
        MailType.REMINDER -> "reminder"
        MailType.SYSTEM -> "system"
        MailType.COLLABORATION -> "collaboration"
        MailType.REPORT -> "report"
        MailType.UPDATE -> "update"
    }

private fun MailType.avatarBackgroundColor(): String =
    when (this) {
        MailType.REMINDER -> "#F2994A"
        MailType.SYSTEM -> "#2D9CDB"
        MailType.COLLABORATION -> "#27AE60"
        MailType.REPORT -> "#EB5757"
        MailType.UPDATE -> "#56CCF2"
    }

private fun Boolean.yesNoText(): String =
    if (this) "Yes" else "No"

private fun String.firstLabel(): String =
    firstOrNull()?.uppercaseChar()?.toString() ?: "M"

private fun formatMailTimestamp(timestampMillis: Long): String =
    requireNotNull(timestampFormatter.get()).format(Date(timestampMillis))

private val timestampFormatter = ThreadLocal.withInitial {
    SimpleDateFormat("HH:mm", Locale.US)
}
