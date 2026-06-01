package com.bytetrain.feishuclone.features.message.domain

data class MessageItem(
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

enum class ConversationType {
    SINGLE,
    GROUP,
    BOT,
}

