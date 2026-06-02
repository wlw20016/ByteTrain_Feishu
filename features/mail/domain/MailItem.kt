package com.bytetrain.feishuclone.features.mail.domain

data class MailItem(
    val id: String,
    val sender: String,
    val subject: String,
    val preview: String,
    val timestampMillis: Long,
    val unread: Boolean,
    val attachmentCount: Int = 0,
    val mailType: MailType = MailType.UPDATE,
    val actionText: String? = null,
)

enum class MailType {
    REMINDER,
    SYSTEM,
    COLLABORATION,
    REPORT,
    UPDATE,
}
