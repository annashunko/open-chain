package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.impl.VoteTransactionEntityConverter
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.component.validator.transaction.impl.VoteTransactionValidator
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.repository.VoteTransactionRepository
import io.openfuture.chain.service.DelegateService
import io.openfuture.chain.service.VoteTransactionService
import io.openfuture.chain.service.WalletService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultVoteTransactionService(
    repository: VoteTransactionRepository,
    walletService: WalletService,
    nodeClock: NodeClock,
    entityConverter: VoteTransactionEntityConverter,
    validator: VoteTransactionValidator,
    private val delegateService: DelegateService
) : DefaultBaseTransactionService<VoteTransaction, VoteTransactionData>(repository,
    walletService, nodeClock, entityConverter, validator), VoteTransactionService {

    @Transactional
    override fun addToBlock(tx: VoteTransaction, block: MainBlock): VoteTransaction {
        val delegate = delegateService.getByPublicKey(tx.delegateKey)
        walletService.changeWalletVote(tx.senderAddress, delegate, tx.getVoteType())
        return super.commonAddToBlock(tx, block)
    }

}