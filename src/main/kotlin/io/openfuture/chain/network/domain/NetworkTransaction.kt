package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
abstract class NetworkTransaction(var timestamp: Long,
                              var amount: Long,
                              var fee: Long,
                              var recipientAddress: String,
                              var senderKey: String,
                              var senderAddress: String,
                              var senderSignature: String,
                              var hash: String) : NetworkEntity() {

    override fun read(buffer: ByteBuf) {
        timestamp = buffer.readLong()
        amount = buffer.readLong()
        fee = buffer.readLong()
        recipientAddress = buffer.readString()
        senderKey = buffer.readString()
        senderAddress = buffer.readString()
        senderSignature = buffer.readString()
        hash = buffer.readString()
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeLong(timestamp)
        buffer.writeLong(amount)
        buffer.writeLong(fee)
        buffer.writeString(recipientAddress)
        buffer.writeString(senderKey)
        buffer.writeString(senderAddress)
        buffer.writeString(senderSignature)
        buffer.writeString(hash)
    }

}