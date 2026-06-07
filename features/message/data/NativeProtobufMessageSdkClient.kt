package com.bytetrain.feishuclone.features.message.data

import com.bytetrain.feishuclone.sdk.NativeRustFeedBridgeClient
import com.bytetrain.feishuclone.sdk.PageInfoWire
import com.bytetrain.feishuclone.sdk.ProtoDecoder
import com.bytetrain.feishuclone.sdk.WIRE_LEN
import com.bytetrain.feishuclone.sdk.WIRE_VARINT
import com.bytetrain.feishuclone.features.message.domain.ConversationType

class NativeProtobufMessageSdkClient(
    private val bridgeClient: NativeRustFeedBridgeClient = NativeRustFeedBridgeClient(),
) : MessageSdkClient {
    override suspend fun getMessagePage(pageSize: Int, cursor: String?): SdkMessagePage {
        val responseBytes = bridgeClient.readMessagePage(pageSize, cursor)
        return decodeMessagePageResponse(responseBytes)
    }
}

private fun decodeMessagePageResponse(bytes: ByteArray): SdkMessagePage {
    val items = mutableListOf<SdkMessageItem>()
    var pageInfo = PageInfoWire()
    val decoder = ProtoDecoder(bytes)

    while (true) {
        val field = decoder.nextField() ?: break
        when (field.number) {
            1 -> {
                field.expectWireType(WIRE_LEN)
                items += decodeMessageItem(field.readBytes())
            }
            2 -> {
                field.expectWireType(WIRE_LEN)
                pageInfo = decodePageInfo(field.readBytes())
            }
            else -> field.skip()
        }
    }

    return SdkMessagePage(
        items = items,
        nextCursor = pageInfo.nextCursor,
        hasMore = pageInfo.hasMore,
    )
}

private fun decodeMessageItem(bytes: ByteArray): SdkMessageItem {
    var id = ""
    var conversationName = ""
    var conversationType = ConversationType.SINGLE
    var avatarUrl: String? = null
    var avatarText = ""
    var lastMessagePreview = ""
    var lastMessageTimeMillis = 0L
    var unreadCount = 0
    var isPinned = false
    var isMuted = false
    var isBot = false
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
                conversationName = field.readString()
            }
            3 -> {
                field.expectWireType(WIRE_VARINT)
                conversationType = conversationTypeFromProto(field.readInt32())
            }
            4 -> {
                field.expectWireType(WIRE_LEN)
                avatarUrl = field.readString().ifEmpty { null }
            }
            5 -> {
                field.expectWireType(WIRE_LEN)
                avatarText = field.readString()
            }
            6 -> {
                field.expectWireType(WIRE_LEN)
                lastMessagePreview = field.readString()
            }
            7 -> {
                field.expectWireType(WIRE_VARINT)
                lastMessageTimeMillis = field.readInt64()
            }
            8 -> {
                field.expectWireType(WIRE_VARINT)
                unreadCount = field.readInt32()
            }
            9 -> {
                field.expectWireType(WIRE_VARINT)
                isPinned = field.readBool()
            }
            10 -> {
                field.expectWireType(WIRE_VARINT)
                isMuted = field.readBool()
            }
            11 -> {
                field.expectWireType(WIRE_VARINT)
                isBot = field.readBool()
            }
            else -> field.skip()
        }
    }

    return SdkMessageItem(
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

private fun conversationTypeFromProto(value: Int): ConversationType =
    when (value) {
        0, 1 -> ConversationType.SINGLE
        2 -> ConversationType.GROUP
        3 -> ConversationType.BOT
        else -> throw IllegalArgumentException("invalid ConversationType enum value $value")
    }
