package com.bytetrain.feishuclone.features.mail.domain

data class MailItem(
    val id: String,
    val sender: String,
    val subject: String,
    val preview: String,
    val timestampMillis: Long,
    val unread: Boolean,
)

