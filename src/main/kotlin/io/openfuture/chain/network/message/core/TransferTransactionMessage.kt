package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
class TransferTransactionMessage(
    timestamp: Long,
    fee: Long,
    hash: String,
    senderAddress: String,
    senderSignature: String,
    senderPublicKey: String,
    var amount: Long,
    var recipientAddress: String
) : BaseTransactionMessage(timestamp, fee, senderAddress, hash, senderSignature, senderPublicKey) {

    constructor(utx: UTransferTransaction) : this(
        utx.timestamp,
        utx.fee,
        utx.senderAddress,
        utx.hash,
        utx.senderSignature,
        utx.senderPublicKey,
        utx.payload.amount,
        utx.payload.recipientAddress
    )

    override fun read(buffer: ByteBuf) {
        super.read(buffer)
        amount = buffer.readLong()
        recipientAddress = buffer.readString()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)
        buffer.writeLong(amount)
        buffer.writeString(recipientAddress)
    }

}
