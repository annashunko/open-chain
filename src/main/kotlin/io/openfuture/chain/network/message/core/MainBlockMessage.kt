package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.*

@NoArgConstructor
class MainBlockMessage(
    height: Long,
    previousHash: String,
    timestamp: Long,
    reward: Long,
    hash: String,
    signature: String,
    publicKey: String,
    var merkleHash: String,
    var voteTransactions: List<VoteTransactionMessage>,
    var delegateTransactions: List<DelegateTransactionMessage>,
    var transferTransactions: List<TransferTransactionMessage>
) : BlockMessage(height, previousHash, timestamp, reward, hash, signature, publicKey) {

    constructor(block: MainBlock, voteTransactions: List<VoteTransaction>, delegateTransactions: List<DelegateTransaction>,
                transferTransactions: List<TransferTransaction>) : this(
        block.height,
        block.previousHash,
        block.timestamp,
        block.reward,
        block.hash,
        block.signature,
        block.publicKey,
        block.payload.merkleHash,
        voteTransactions.map { VoteTransactionMessage(it) },
        delegateTransactions.map { DelegateTransactionMessage(it) },
        transferTransactions.map { TransferTransactionMessage(it) }
    )

    fun getAllTransactions(): List<BaseTransactionMessage> {
        return voteTransactions + delegateTransactions + transferTransactions
    }

    override fun read(buffer: ByteBuf) {
        super.read(buffer)

        merkleHash = buffer.readString()
        voteTransactions = buffer.readList()
        delegateTransactions = buffer.readList()
        transferTransactions = buffer.readList()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)

        buffer.writeString(merkleHash)
        buffer.writeList(voteTransactions)
        buffer.writeList(delegateTransactions)
        buffer.writeList(transferTransactions)
    }

    override fun toString() = "MainBlockMessage(hash=$hash)"

}
