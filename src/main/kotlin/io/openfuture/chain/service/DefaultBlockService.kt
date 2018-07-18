package io.openfuture.chain.service

import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.BlockRepository
import io.openfuture.chain.repository.GenesisBlockRepository
import io.openfuture.chain.repository.MainBlockRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.annotation.PostConstruct

@Service
class DefaultBlockService(
    private val blockRepository: BlockRepository<Block>,
    private val mainBlockRepository: MainBlockRepository,
    private val genesisBlockRepository: GenesisBlockRepository,
    private val transactionService: BaseTransactionService<BaseTransaction>,
    private val walletService: WalletService
) : BlockService {

    @PostConstruct
    fun init(){
        println("1")

        val block1 = blockRepository.findByHash("111")

        println("1")

        val block2 = blockRepository.findByHash("111")

        println("2")
    }

    @Transactional(readOnly = true)
    override fun get(hash: String): Block = blockRepository.findByHash(hash)
        ?: throw NotFoundException("Block with hash:$hash not found ")


    @Transactional(readOnly = true)
    override fun getLast(): Block =
        blockRepository.findFirstByOrderByHeightDesc()
            ?: throw NotFoundException("Last block not found!")

    @Transactional(readOnly = true)
    override fun getLastMain(): MainBlock =
        mainBlockRepository.findFirstByOrderByHeightDesc()
            ?: throw NotFoundException("Last Main block not found!")

    @Transactional(readOnly = true)
    override fun getLastGenesis(): GenesisBlock =
        genesisBlockRepository.findFirstByOrderByHeightDesc()
            ?: throw NotFoundException("Last Genesis block not exist!")

    @Transactional
    override fun save(block: MainBlock): MainBlock {
        val savedBlock = mainBlockRepository.save(block)
        val transactions = block.transactions
        for (transaction in transactions) {
            transactionService.addToBlock(transaction.hash, savedBlock)
            walletService.updateByTransaction(transaction)
        }
        return savedBlock
    }

    @Transactional
    override fun save(block: GenesisBlock): GenesisBlock {
        return genesisBlockRepository.save(block)
    }

}