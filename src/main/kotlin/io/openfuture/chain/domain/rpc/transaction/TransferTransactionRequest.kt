package io.openfuture.chain.domain.rpc.transaction

import io.openfuture.chain.domain.transaction.data.TransferTransactionData
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.entity.transaction.unconfirmed.UVoteTransaction
import io.openfuture.chain.util.TransactionUtils

class TransferTransactionRequest(
    data: TransferTransactionData
) : BaseTransactionRequest<UTransferTransaction, TransferTransactionData>(data) {

    override fun toEntity(timestamp: Long): UTransferTransaction = UTransferTransaction(
        timestamp,
        data!!.amount,
        data!!.fee,
        data!!.recipientAddress,
        data!!.senderAddress,
        senderPublicKey!!,
        senderSignature!!,
        TransactionUtils.createHash(data!!, senderPublicKey!!, senderSignature!!)
    )

}