package com.bytetrain.feishuclone.features.message.domain

data class MessageItem(
    val id: String,
    val title: String,
    val summary: String,
    val timestampMillis: Long,
    val unread: Boolean,
)

