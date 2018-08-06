package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.core.service.TransactionService
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.core.service.VoteTransactionService
import io.openfuture.chain.network.message.core.BaseTransactionMessage
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.network.message.core.VoteTransactionMessage
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultTransactionService(
    private val repository: TransactionRepository<Transaction>,
    private val unconfirmedRepository: UTransactionRepository<UTransaction>,
    private val voteTransactionService: VoteTransactionService,
    private val transferTransactionService: TransferTransactionService,
    private val delegateTransactionService: DelegateTransactionService
) : TransactionService {

    @Transactional(readOnly = true)
    override fun getAllUnconfirmed(): MutableList<UTransaction> {
        return unconfirmedRepository.findAllByOrderByFeeDesc()
    }

    @Transactional(readOnly = true)
    override fun getUnconfirmedByHash(hash: String): UTransaction = unconfirmedRepository.findOneByHash(hash)
        ?: throw NotFoundException("Transaction  with hash $hash not found")

    @Transactional(readOnly = true)
    override fun getCount(): Long {
        return repository.count()
    }

    @Transactional
    override fun add(message: BaseTransactionMessage): UTransaction {
         return when (message) {
            is VoteTransactionMessage -> voteTransactionService.add(message)
            is TransferTransactionMessage -> transferTransactionService.add(message)
            is DelegateTransactionMessage -> delegateTransactionService.add(message)
            else -> throw IllegalStateException("Unknown transaction type")
        }
    }

    @Transactional
    override fun synchronize(message: BaseTransactionMessage, block: MainBlock) {
        return when (message) {
            is VoteTransactionMessage -> voteTransactionService.synchronize(message, block)
            is TransferTransactionMessage -> transferTransactionService.synchronize(message, block)
            is DelegateTransactionMessage -> delegateTransactionService.synchronize(message, block)
            else -> throw IllegalStateException("Unknown transaction type")
        }
    }

    @Transactional
    override fun toBlock(utx: UTransaction, block: MainBlock): Transaction {
        return when (utx) {
            is UnconfirmedVoteTransaction -> voteTransactionService.toBlock(utx, block)
            is UTransferTransaction -> transferTransactionService.toBlock(utx, block)
            is UDelegateTransaction -> delegateTransactionService.toBlock(utx, block)
            else -> throw IllegalStateException("Unknown transaction type")
        }
    }

}