package io.openfuture.chain.component.converter.transaction.impl

import io.openfuture.chain.component.converter.transaction.ManualTransactionEntityConverter
import io.openfuture.chain.crypto.key.NodeKeyHolder
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.DelegateTransactionData
import io.openfuture.chain.entity.transaction.DelegateTransaction
import org.springframework.stereotype.Component

@Component
class DelegateTransactionEntityConverter(
    private val keyHolder: NodeKeyHolder
) : BaseTransactionEntityConverter<DelegateTransactionData>(),
    ManualTransactionEntityConverter<DelegateTransaction, DelegateTransactionData> {

    override fun toEntity(dto: BaseTransactionDto<DelegateTransactionData>): DelegateTransaction = DelegateTransaction(
        dto.timestamp,
        dto.data.amount,
        dto.data.fee,
        dto.data.recipientAddress,
        dto.data.senderAddress,
        dto.senderPublicKey,
        dto.senderSignature,
        dto.hash,
        dto.data.delegateKey
    )

    override fun toEntity(timestamp: Long, request: BaseTransactionRequest<DelegateTransactionData>): DelegateTransaction =
        DelegateTransaction(
            timestamp,
            request.data!!.amount,
            request.data!!.fee,
            request.data!!.recipientAddress,
            request.data!!.senderAddress,
            request.senderPublicKey!!,
            request.senderSignature!!,
            getHash(request),
            request.data!!.delegateKey
        )

    override fun toEntity(timestamp: Long, data: DelegateTransactionData): DelegateTransaction =
        DelegateTransaction(
            timestamp,
            data.amount,
            data.fee,
            data.recipientAddress,
            data.senderAddress,
            HashUtils.toHexString(keyHolder.getPublicKey()),
            getSignature(data, keyHolder.getPrivateKey()),
            getHash(data, keyHolder.getPublicKey(), keyHolder.getPrivateKey()),
            data.delegateKey
        )

}