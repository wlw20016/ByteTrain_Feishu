package com.bytetrain.feishuclone.sdk

interface RustFeedBridge {
    fun readMessagePage(requestBytes: ByteArray): ByteArray
    fun readMailPage(requestBytes: ByteArray): ByteArray
}

object NativeRustFeedBridge : RustFeedBridge {
    init {
        System.loadLibrary("bytetrain_feed_sdk")
    }

    override fun readMessagePage(requestBytes: ByteArray): ByteArray =
        readMessagePageNative(requestBytes).decodeBridgeEnvelope()

    override fun readMailPage(requestBytes: ByteArray): ByteArray =
        readMailPageNative(requestBytes).decodeBridgeEnvelope()

    private external fun readMessagePageNative(requestBytes: ByteArray): ByteArray
    private external fun readMailPageNative(requestBytes: ByteArray): ByteArray
}

class NativeRustFeedBridgeClient(
    private val bridge: RustFeedBridge = NativeRustFeedBridge,
) {
    fun readMessagePage(pageSize: Int, cursor: String?): ByteArray =
        bridge.readMessagePage(FeedProtobuf.encodePageRequest(pageSize, cursor))

    fun readMailPage(pageSize: Int, cursor: String?): ByteArray =
        bridge.readMailPage(FeedProtobuf.encodePageRequest(pageSize, cursor))
}

fun ByteArray.decodeBridgeEnvelope(): ByteArray {
    if (isEmpty()) {
        throw SdkBridgeException("native bridge returned an empty response", BridgeErrorCode.NATIVE_BRIDGE)
    }

    return when (this[0].toInt() and 0xff) {
        0 -> copyOfRange(1, size)
        1 -> decodeBridgeError()
        else -> throw SdkBridgeException(
            "native bridge returned an unknown envelope tag ${(this[0].toInt() and 0xff)}",
            BridgeErrorCode.NATIVE_BRIDGE,
        )
    }
}

private fun ByteArray.decodeBridgeError(): Nothing {
    if (size < 6) {
        throw SdkBridgeException("native bridge returned a malformed error envelope", BridgeErrorCode.NATIVE_BRIDGE)
    }

    val code = BridgeErrorCode.fromWireCode(this[1].toInt() and 0xff)
    val messageLength = ((this[2].toInt() and 0xff) shl 24) or
        ((this[3].toInt() and 0xff) shl 16) or
        ((this[4].toInt() and 0xff) shl 8) or
        (this[5].toInt() and 0xff)
    val messageEnd = 6 + messageLength
    if (messageLength < 0 || messageEnd > size) {
        throw SdkBridgeException("native bridge returned a malformed error message", BridgeErrorCode.NATIVE_BRIDGE)
    }

    val message = copyOfRange(6, messageEnd).decodeToString()
    throw SdkBridgeException(message, code)
}
