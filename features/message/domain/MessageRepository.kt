package com.bytetrain.feishuclone.features.message.domain

interface MessageRepository {
    suspend fun loadPage(pageSize: Int, cursor: String?): MessagePage
}

data class MessagePage(
    val items: List<MessageItem>,
    val nextCursor: String?,
    val hasMore: Boolean,
)

