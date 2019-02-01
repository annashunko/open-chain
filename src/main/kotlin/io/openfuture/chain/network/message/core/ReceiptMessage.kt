package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.crypto.util.HashUtils.sha256
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.serialization.Serializable
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.nio.ByteBuffer
import kotlin.text.Charsets.UTF_8

@NoArgConstructor
class ReceiptMessage(
    var transactionHash: String,
    var result: String
) : Serializable {

    override fun read(buf: ByteBuf) {
        transactionHash = buf.readString()
        result = buf.readString()
    }

    override fun write(buf: ByteBuf) {
        buf.writeString(transactionHash)
        buf.writeString(result)
    }

    fun getHash(): String {
        val txHashBytes = transactionHash.toByteArray(UTF_8)
        val resultBytes = result.toByteArray(UTF_8)
        val bytes = ByteBuffer.allocate(txHashBytes.size + resultBytes.size)
            .put(txHashBytes)
            .put(resultBytes)
            .array()

        return ByteUtils.toHexString(sha256(bytes))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ReceiptMessage) return false

        if (transactionHash != other.transactionHash) return false
        if (result != other.result) return false

        return true
    }

    override fun hashCode(): Int {
        var result1 = transactionHash.hashCode()
        result1 = 31 * result1 + result.hashCode()
        return result1
    }

}