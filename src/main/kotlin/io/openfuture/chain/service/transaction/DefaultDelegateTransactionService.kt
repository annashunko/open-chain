package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.impl.DelegateTransactionEntityConverter
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.component.validator.transaction.impl.DelegateTransactionValidator
import io.openfuture.chain.domain.transaction.data.DelegateTransactionData
import io.openfuture.chain.entity.Delegate
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.DelegateTransaction
import io.openfuture.chain.repository.DelegateTransactionRepository
import io.openfuture.chain.service.DelegateService
import io.openfuture.chain.service.DelegateTransactionService
import io.openfuture.chain.service.WalletService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultDelegateTransactionService(
    repository: DelegateTransactionRepository,
    walletService: WalletService,
    nodeClock: NodeClock,
    entityConverter: DelegateTransactionEntityConverter,
    validator: DelegateTransactionValidator,
    private val delegateService: DelegateService
) : DefaultBaseTransactionService<DelegateTransaction, DelegateTransactionData>(repository, walletService, nodeClock,
    entityConverter, validator), DelegateTransactionService {

    @Transactional
    override fun addToBlock(tx: DelegateTransaction, block: MainBlock): DelegateTransaction {
        delegateService.save(Delegate(tx.delegateKey, tx.senderAddress))
        return super.commonAddToBlock(tx, block)
    }

}