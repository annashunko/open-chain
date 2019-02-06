package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import org.apache.commons.lang3.StringUtils.EMPTY
import java.nio.ByteBuffer
import kotlin.text.Charsets.UTF_8

@NoArgConstructor
class WalletStateMessage(
    address: String,
    var balance: Long,
    var voteFor: String? = null
) : StateMessage(address) {

    override fun getBytes(): ByteArray {
        val voteForTemp = voteFor ?: ""
        return ByteBuffer.allocate(LONG_BYTES + voteForTemp.toByteArray(UTF_8).size)
            .putLong(balance)
            .put(voteForTemp.toByteArray(UTF_8))
            .array()
    }

    override fun read(buf: ByteBuf) {
        super.read(buf)
        balance = buf.readLong()
        val voteForTemp = buf.readString()
        if (voteForTemp != EMPTY) {
            voteFor = voteForTemp
        }
    }

    override fun write(buf: ByteBuf) {
        super.write(buf)
        buf.writeLong(balance)
        buf.writeString(voteFor ?: "")
    }

}