package io.openfuture.chain.service

import io.openfuture.chain.entity.Block
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.BlockRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultBlockService(
    private val blockRepository: BlockRepository
) : BlockService {

    @Transactional(readOnly = true)
    override fun get(hash: String): Block = blockRepository.findByHash(hash)
        ?: throw NotFoundException("Block with hash:$hash not found")

    @Transactional(readOnly = true)
    override fun getLast(): Block = blockRepository.findFirstByOrderByHeightDesc()
        ?: throw NotFoundException("Last block not found!")

    @Transactional(readOnly = true)
    override fun getLastGenesis(): Block {
        TODO("not implemented")
    }

}