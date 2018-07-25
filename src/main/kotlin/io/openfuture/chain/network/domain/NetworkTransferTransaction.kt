package io.openfuture.chain.network.domain

import io.openfuture.chain.annotation.NoArgConstructor
import io.openfuture.chain.entity.transaction.TransferTransaction

@NoArgConstructor
class NetworkTransferTransaction(timestamp: Long = 0,
                                 amount: Double = 0.0,
                                 fee: Double = 0.0,
                                 recipientAddress: String,
                                 senderKey: String,
                                 senderAddress: String,
                                 senderSignature: String,
                                 hash: String) : NetworkTransaction(timestamp, amount, fee, recipientAddress,
    senderKey, senderAddress, senderSignature, hash) {

    constructor(transaction: TransferTransaction) : this(transaction.timestamp, transaction.amount,
        transaction.fee, transaction.recipientAddress, transaction.senderKey, transaction.senderAddress,
        transaction.senderSignature!!, transaction.hash)

}
