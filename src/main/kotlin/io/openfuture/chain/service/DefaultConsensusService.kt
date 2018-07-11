package io.openfuture.chain.service

import io.openfuture.chain.property.ConsensusProperties
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultConsensusService(
    private val consensusProperties: ConsensusProperties,
    private val blockService: BlockService
) : ConsensusService {

    @Transactional(readOnly = true)
    override fun getCurrentEpochHeight(): Int {
        val newHeight = blockService.getLast().height
        val lastGenesisBlockHeight = blockService.getLastGenesis().height

        return newHeight - lastGenesisBlockHeight
    }

    @Transactional(readOnly = true)
    override fun isGenesisBlockNeeded(): Boolean {
        return (consensusProperties.epochHeight!! - 1) <= getCurrentEpochHeight()
    }

}