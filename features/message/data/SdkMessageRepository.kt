package com.bytetrain.feishuclone.features.message.data

import com.bytetrain.feishuclone.features.message.domain.ConversationType
import com.bytetrain.feishuclone.features.message.domain.MessageItem
import com.bytetrain.feishuclone.features.message.domain.MessagePage
import com.bytetrain.feishuclone.features.message.domain.MessageRepository

class SdkMessageRepository(
    private val sdkClient: MessageSdkClient,
    private val fallbackRepository: MessageRepository? = null,
) : MessageRepository {
    override suspend fun loadPage(pageSize: Int, cursor: String?): MessagePage =
        try {
            val page = sdkClient.getMessagePage(pageSize, cursor)
            MessagePage(
                items = page.items.map { it.toDomain() },
                nextCursor = page.nextCursor,
                hasMore = page.hasMore,
            )
        } catch (error: Throwable) {
            fallbackRepository?.loadPage(pageSize, cursor) ?: throw error
        }
}

interface MessageSdkClient {
    suspend fun getMessagePage(pageSize: Int, cursor: String?): SdkMessagePage
}

data class SdkMessagePage(
    val items: List<SdkMessageItem>,
    val nextCursor: String?,
    val hasMore: Boolean,
)

data class SdkMessageItem(
    val id: String,
    val conversationName: String,
    val conversationType: ConversationType,
    val avatarUrl: String?,
    val avatarText: String,
    val lastMessagePreview: String,
    val lastMessageTimeMillis: Long,
    val unreadCount: Int,
    val isPinned: Boolean,
    val isMuted: Boolean,
    val isBot: Boolean,
)

private fun SdkMessageItem.toDomain(): MessageItem =
    MessageItem(
        id = id,
        conversationName = conversationName,
        conversationType = conversationType,
        avatarUrl = avatarUrl,
        avatarText = avatarText,
        lastMessagePreview = lastMessagePreview,
        lastMessageTimeMillis = lastMessageTimeMillis,
        unreadCount = unreadCount,
        isPinned = isPinned,
        isMuted = isMuted,
        isBot = isBot,
    )
