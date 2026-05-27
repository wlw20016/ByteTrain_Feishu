package com.bytetrain.feishuclone.features.mail.domain

interface MailRepository {
    suspend fun loadPage(pageSize: Int, cursor: String?): MailPage
}

data class MailPage(
    val items: List<MailItem>,
    val nextCursor: String?,
    val hasMore: Boolean,
)

