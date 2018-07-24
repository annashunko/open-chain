package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.impl.VoteTransactionEntityConverter
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.property.ConsensusProperties
import io.openfuture.chain.repository.VoteTransactionRepository
import io.openfuture.chain.service.DelegateService
import io.openfuture.chain.service.VoteTransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.xml.bind.ValidationException

@Service
class DefaultVoteTransactionService(
    repository: VoteTransactionRepository,
    entityConverter: VoteTransactionEntityConverter,
    private val consensusProperties: ConsensusProperties,
    private val delegateService: DelegateService
) : DefaultManualTransactionService<VoteTransaction, VoteTransactionData>(repository, entityConverter),
    VoteTransactionService {

    @Transactional
    override fun toBlock(tx: VoteTransaction, block: MainBlock): VoteTransaction {
        updateWalletVotes(tx)
        return baseToBlock(tx, block)
    }

    private fun updateWalletVotes(tx: VoteTransaction) {
        val delegate = delegateService.getByPublicKey(tx.delegateKey)
        val wallet = walletService.getByAddress(tx.senderAddress)

        when (tx.getVoteType()) {
            VoteType.FOR -> {
                wallet.votes.add(delegate)
            }
            VoteType.AGAINST -> {
                wallet.votes.remove(delegate)
            }
        }
        walletService.save(wallet)
    }

    @Transactional
    override fun validate(dto: BaseTransactionDto<VoteTransactionData>) {
        if (!isValidVoteCount(dto.data.senderAddress)) {
            throw ValidationException("Wallet ${dto.data.senderAddress} already spent all votes!")
        }
        baseValidate(dto)
    }

    @Transactional
    override fun validate(request: BaseTransactionRequest<VoteTransactionData>) {
        if (!isValidVoteCount(request.data!!.senderAddress)) {
            throw ValidationException("Wallet ${request.data!!.senderAddress} already spent all votes!")
        }
        baseValidate(request)
    }

    private fun isValidVoteCount(senderAddress: String): Boolean {
        val confirmedVotes = walletService.getVotesByAddress(senderAddress).count()
        val unconfirmedForVotes = getAllPending()
            .filter { it.senderAddress == senderAddress && it.getVoteType() == VoteType.FOR }
            .count()

        val unspentVotes = confirmedVotes + unconfirmedForVotes
        if (consensusProperties.delegatesCount!! <= unspentVotes) {
            return false
        }
        return true
    }

}