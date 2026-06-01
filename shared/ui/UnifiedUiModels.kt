package com.bytetrain.feishuclone.shared.ui

data class UnifiedListItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val timestampText: String,
    val avatar: AvatarModel,
    val badges: List<BadgeModel> = emptyList(),
    val displayStyle: DisplayStyle,
    val detail: DetailModel,
)

data class AvatarModel(
    val label: String,
    val imageUrl: String? = null,
    val backgroundColor: String? = null,
)

data class BadgeModel(
    val text: String,
    val tone: String,
)

enum class DisplayStyle {
    DENSE_CONVERSATION,
    MAIL_CARD,
}

data class DetailModel(
    val id: String,
    val title: String,
    val body: String,
    val metas: List<DetailMeta> = emptyList(),
)

data class DetailMeta(
    val label: String,
    val value: String,
)
