package com.bytetrain.feishuclone.sdk

import java.io.ByteArrayOutputStream

object FeedProtobuf {
    fun encodePageRequest(pageSize: Int, cursor: String?): ByteArray {
        val output = ByteArrayOutputStream()
        writeInt32Field(output, 1, pageSize)
        if (cursor != null) {
            writeStringField(output, 2, cursor)
        }
        return output.toByteArray()
    }

    internal fun encodeBridgeError(code: BridgeErrorCode, message: String): ByteArray {
        val messageBytes = message.encodeToByteArray()
        val output = ByteArrayOutputStream()
        output.write(1)
        output.write(code.wireCode)
        output.write((messageBytes.size ushr 24) and 0xff)
        output.write((messageBytes.size ushr 16) and 0xff)
        output.write((messageBytes.size ushr 8) and 0xff)
        output.write(messageBytes.size and 0xff)
        output.write(messageBytes)
        return output.toByteArray()
    }

    private fun writeInt32Field(output: ByteArrayOutputStream, number: Int, value: Int) {
        writeKey(output, number, WIRE_VARINT)
        writeVarint(output, value.toLong())
    }

    private fun writeStringField(output: ByteArrayOutputStream, number: Int, value: String) {
        val bytes = value.encodeToByteArray()
        writeKey(output, number, WIRE_LEN)
        writeVarint(output, bytes.size.toLong())
        output.write(bytes)
    }

    private fun writeKey(output: ByteArrayOutputStream, number: Int, wireType: Int) {
        writeVarint(output, (number.toLong() shl 3) or wireType.toLong())
    }

    private fun writeVarint(output: ByteArrayOutputStream, value: Long) {
        var remaining = value
        while (remaining >= 0x80) {
            output.write(((remaining and 0x7f) or 0x80).toInt())
            remaining = remaining ushr 7
        }
        output.write(remaining.toInt())
    }
}

class ProtoDecoder(
    private val bytes: ByteArray,
) {
    private var offset = 0

    fun nextField(): ProtoField? {
        if (offset == bytes.size) {
            return null
        }

        val key = readVarint()
        val number = (key ushr 3).toInt()
        val wireType = (key and 0b111).toInt()
        if (number == 0) {
            throw SdkBridgeException("invalid protobuf field number 0", BridgeErrorCode.PROTOBUF_DECODE)
        }

        val start = offset
        when (wireType) {
            WIRE_VARINT -> readVarint()
            WIRE_LEN -> {
                val length = readVarint().toInt()
                val end = offset + length
                if (length < 0 || end < offset || end > bytes.size) {
                    throw SdkBridgeException("invalid protobuf length", BridgeErrorCode.PROTOBUF_DECODE)
                }
                offset = end
            }
            else -> throw SdkBridgeException(
                "unsupported protobuf wire type $wireType for field $number",
                BridgeErrorCode.PROTOBUF_DECODE,
            )
        }

        return ProtoField(number, wireType, bytes.copyOfRange(start, offset))
    }

    private fun readVarint(): Long {
        var result = 0L
        var shift = 0
        while (shift < 64) {
            val byte = bytes.getOrNull(offset)?.toInt()?.and(0xff)
                ?: throw SdkBridgeException("invalid protobuf varint", BridgeErrorCode.PROTOBUF_DECODE)
            offset += 1
            result = result or ((byte and 0x7f).toLong() shl shift)
            if ((byte and 0x80) == 0) {
                return result
            }
            shift += 7
        }
        throw SdkBridgeException("invalid protobuf varint", BridgeErrorCode.PROTOBUF_DECODE)
    }
}

class ProtoField(
    val number: Int,
    private val wireType: Int,
    private val bytes: ByteArray,
) {
    fun expectWireType(expected: Int) {
        if (wireType != expected) {
            throw SdkBridgeException(
                "invalid wire type for field $number: expected $expected actual $wireType",
                BridgeErrorCode.PROTOBUF_DECODE,
            )
        }
    }

    fun readString(): String = readBytes().decodeToString()

    fun readBytes(): ByteArray {
        var offset = 0
        val length = readVarint(bytes, offset).also { offset = it.nextOffset }.value.toInt()
        val end = offset + length
        if (length < 0 || end != bytes.size) {
            throw SdkBridgeException("invalid protobuf length", BridgeErrorCode.PROTOBUF_DECODE)
        }
        return bytes.copyOfRange(offset, end)
    }

    fun readInt32(): Int {
        val result = readVarint(bytes, 0)
        if (result.nextOffset != bytes.size || result.value > Int.MAX_VALUE) {
            throw SdkBridgeException("invalid protobuf int32", BridgeErrorCode.PROTOBUF_DECODE)
        }
        return result.value.toInt()
    }

    fun readInt64(): Long {
        val result = readVarint(bytes, 0)
        if (result.nextOffset != bytes.size) {
            throw SdkBridgeException("invalid protobuf int64", BridgeErrorCode.PROTOBUF_DECODE)
        }
        return result.value
    }

    fun readBool(): Boolean {
        val result = readVarint(bytes, 0)
        if (result.nextOffset != bytes.size) {
            throw SdkBridgeException("invalid protobuf bool", BridgeErrorCode.PROTOBUF_DECODE)
        }
        return result.value != 0L
    }

    fun skip() {
        // The decoder has already consumed unknown field bytes.
    }
}

enum class BridgeErrorCode(val wireCode: Int) {
    INVALID_PAGE_SIZE(1),
    INVALID_CURSOR(2),
    CURSOR_OUT_OF_RANGE(3),
    PROTOBUF_DECODE(4),
    PROTOBUF_ENCODE(5),
    SDK_READ(6),
    NATIVE_BRIDGE(7),
    UNKNOWN(255);

    companion object {
        fun fromWireCode(value: Int): BridgeErrorCode =
            entries.firstOrNull { it.wireCode == value } ?: UNKNOWN
    }
}

class SdkBridgeException(
    message: String,
    val code: BridgeErrorCode = BridgeErrorCode.UNKNOWN,
) : IllegalStateException(message)

data class PageInfoWire(
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
)

private data class VarintResult(
    val value: Long,
    val nextOffset: Int,
)

private fun readVarint(bytes: ByteArray, startOffset: Int): VarintResult {
    var offset = startOffset
    var result = 0L
    var shift = 0
    while (shift < 64) {
        val byte = bytes.getOrNull(offset)?.toInt()?.and(0xff)
            ?: throw SdkBridgeException("invalid protobuf varint", BridgeErrorCode.PROTOBUF_DECODE)
        offset += 1
        result = result or ((byte and 0x7f).toLong() shl shift)
        if ((byte and 0x80) == 0) {
            return VarintResult(result, offset)
        }
        shift += 7
    }
    throw SdkBridgeException("invalid protobuf varint", BridgeErrorCode.PROTOBUF_DECODE)
}

const val WIRE_VARINT = 0
const val WIRE_LEN = 2
