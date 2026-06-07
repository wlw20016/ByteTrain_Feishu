package com.bytetrain.feishuclone

import com.bytetrain.feishuclone.features.mail.data.MailSdkClient
import com.bytetrain.feishuclone.features.mail.data.MockMailRepository
import com.bytetrain.feishuclone.features.mail.data.NativeProtobufMailSdkClient
import com.bytetrain.feishuclone.features.mail.data.RuntimeMailSdkClient
import com.bytetrain.feishuclone.features.mail.data.SdkMailRepository
import com.bytetrain.feishuclone.features.mail.domain.MailRepository
import com.bytetrain.feishuclone.features.message.data.MessageSdkClient
import com.bytetrain.feishuclone.features.message.data.MockMessageRepository
import com.bytetrain.feishuclone.features.message.data.NativeProtobufMessageSdkClient
import com.bytetrain.feishuclone.features.message.data.RuntimeMessageSdkClient
import com.bytetrain.feishuclone.features.message.data.SdkMessageRepository
import com.bytetrain.feishuclone.features.message.domain.MessageRepository

class AppRepositoryProvider(
    private val sdkRuntimeEnabled: Boolean = true,
    private val messageFallbackFactory: () -> MessageRepository = { MockMessageRepository() },
    private val mailFallbackFactory: () -> MailRepository = { MockMailRepository() },
    private val messageSdkClientFactory: () -> MessageSdkClient = { NativeProtobufMessageSdkClient() },
    private val mailSdkClientFactory: () -> MailSdkClient = { NativeProtobufMailSdkClient() },
) {
    fun createMessageRepository(): MessageRepository {
        val fallbackRepository = messageFallbackFactory()
        if (!sdkRuntimeEnabled) {
            return fallbackRepository
        }

        return try {
            SdkMessageRepository(
                sdkClient = messageSdkClientFactory(),
                fallbackRepository = fallbackRepository,
            )
        } catch (error: Throwable) {
            fallbackRepository
        }
    }

    fun createMailRepository(): MailRepository {
        val fallbackRepository = mailFallbackFactory()
        if (!sdkRuntimeEnabled) {
            return fallbackRepository
        }

        return try {
            SdkMailRepository(
                sdkClient = mailSdkClientFactory(),
                fallbackRepository = fallbackRepository,
            )
        } catch (error: Throwable) {
            fallbackRepository
        }
    }

    companion object {
        fun withRuntimeFakeFallback(): AppRepositoryProvider =
            AppRepositoryProvider(
                messageSdkClientFactory = { RuntimeMessageSdkClient() },
                mailSdkClientFactory = { RuntimeMailSdkClient() },
            )
    }
}
