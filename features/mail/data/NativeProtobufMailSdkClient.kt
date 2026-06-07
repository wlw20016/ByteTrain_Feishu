package com.bytetrain.feishuclone.features.mail.data

import com.bytetrain.feishuclone.sdk.NativeRustFeedBridgeClient
import com.bytetrain.feishuclone.sdk.PageInfoWire
import com.bytetrain.feishuclone.sdk.ProtoDecoder
import com.bytetrain.feishuclone.sdk.WIRE_LEN
import com.bytetrain.feishuclone.sdk.WIRE_VARINT
import com.bytetrain.feishuclone.features.mail.domain.MailType

class NativeProtobufMailSdkClient(
    private val bridgeClient: NativeRustFeedBridgeClient = NativeRustFeedBridgeClient(),
) : MailSdkClient {
    override suspend fun getMailPage(pageSize: Int, cursor: String?): SdkMailPage {
        val responseBytes = bridgeClient.readMailPage(pageSize, cursor)
        return decodeMailPageResponse(responseBytes)
    }
}

private fun decodeMailPageResponse(bytes: ByteArray): SdkMailPage {
    val items = mutableListOf<SdkMailItem>()
    var pageInfo = PageInfoWire()
    val decoder = ProtoDecoder(bytes)

    while (true) {
        val field = decoder.nextField() ?: break
        when (field.number) {
            1 -> {
                field.expectWireType(WIRE_LEN)
                items += decodeMailItem(field.readBytes())
            }
            2 -> {
                field.expectWireType(WIRE_LEN)
                pageInfo = decodePageInfo(field.readBytes())
            }
            else -> field.skip()
        }
    }

    return SdkMailPage(
        items = items,
        nextCursor = pageInfo.nextCursor,
        hasMore = pageInfo.hasMore,
    )
}

private fun decodeMailItem(bytes: ByteArray): SdkMailItem {
    var id = ""
    var sender = ""
    var subject = ""
    var preview = ""
    var timestampMillis = 0L
    var unread = false
    var attachmentCount = 0
    var mailType = MailType.UPDATE
    var actionText: String? = null
    val decoder = ProtoDecoder(bytes)

    while (true) {
        val field = decoder.nextField() ?: break
        when (field.number) {
            1 -> {
                field.expectWireType(WIRE_LEN)
                id = field.readString()
            }
            2 -> {
                field.expectWireType(WIRE_LEN)
                sender = field.readString()
            }
            3 -> {
                field.expectWireType(WIRE_LEN)
                subject = field.readString()
            }
            4 -> {
                field.expectWireType(WIRE_LEN)
                preview = field.readString()
            }
            5 -> {
                field.expectWireType(WIRE_VARINT)
                timestampMillis = field.readInt64()
            }
            6 -> {
                field.expectWireType(WIRE_VARINT)
                unread = field.readBool()
            }
            7 -> {
                field.expectWireType(WIRE_VARINT)
                attachmentCount = field.readInt32()
            }
            8 -> {
                field.expectWireType(WIRE_VARINT)
                mailType = mailTypeFromProto(field.readInt32())
            }
            9 -> {
                field.expectWireType(WIRE_LEN)
                actionText = field.readString().ifEmpty { null }
            }
            else -> field.skip()
        }
    }

    return SdkMailItem(
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
}

private fun decodePageInfo(bytes: ByteArray): PageInfoWire {
    var pageInfo = PageInfoWire()
    val decoder = ProtoDecoder(bytes)

    while (true) {
        val field = decoder.nextField() ?: break
        when (field.number) {
            1 -> {
                field.expectWireType(WIRE_LEN)
                pageInfo = pageInfo.copy(nextCursor = field.readString().ifEmpty { null })
            }
            2 -> {
                field.expectWireType(WIRE_VARINT)
                pageInfo = pageInfo.copy(hasMore = field.readBool())
            }
            else -> field.skip()
        }
    }

    return pageInfo
}

private fun mailTypeFromProto(value: Int): MailType =
    when (value) {
        1 -> MailType.REMINDER
        2 -> MailType.SYSTEM
        3 -> MailType.COLLABORATION
        4 -> MailType.REPORT
        0, 5 -> MailType.UPDATE
        else -> throw IllegalArgumentException("invalid MailType enum value $value")
    }
