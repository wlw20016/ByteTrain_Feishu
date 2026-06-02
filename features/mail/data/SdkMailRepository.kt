package com.bytetrain.feishuclone.features.mail.data

import com.bytetrain.feishuclone.features.mail.domain.MailItem
import com.bytetrain.feishuclone.features.mail.domain.MailPage
import com.bytetrain.feishuclone.features.mail.domain.MailRepository
import com.bytetrain.feishuclone.features.mail.domain.MailType

class SdkMailRepository(
    private val sdkClient: MailSdkClient,
    private val fallbackRepository: MailRepository? = null,
) : MailRepository {
    override suspend fun loadPage(pageSize: Int, cursor: String?): MailPage =
        try {
            val page = sdkClient.getMailPage(pageSize, cursor)
            MailPage(
                items = page.items.map { it.toDomain() },
                nextCursor = page.nextCursor,
                hasMore = page.hasMore,
            )
        } catch (error: Throwable) {
            fallbackRepository?.loadPage(pageSize, cursor) ?: throw error
        }
}

interface MailSdkClient {
    suspend fun getMailPage(pageSize: Int, cursor: String?): SdkMailPage
}

data class SdkMailPage(
    val items: List<SdkMailItem>,
    val nextCursor: String?,
    val hasMore: Boolean,
)

data class SdkMailItem(
    val id: String,
    val sender: String,
    val subject: String,
    val preview: String,
    val timestampMillis: Long,
    val unread: Boolean,
    val attachmentCount: Int,
    val mailType: MailType,
    val actionText: String?,
)

private fun SdkMailItem.toDomain(): MailItem =
    MailItem(
        id = id,
        sender = sender,
        subject = subject,
        preview = preview,
        timestampMillis = timestampMillis,
        unread = unread,
        attachmentCount = attachmentCount,
        mailType = mailType,
        actionText = actionText,
    )
