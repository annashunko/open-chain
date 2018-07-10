package io.openfuture.chain.nio.converter

import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.protocol.CommunicationProtocol
import org.springframework.stereotype.Component

@Component
class GenesisBlockConverter {

    fun toGenesisBlockProto(block: Block): CommunicationProtocol.GenesisBlock {
        val genesisBlock = block as GenesisBlock
        return CommunicationProtocol.GenesisBlock.newBuilder()
            .setHash(genesisBlock.hash)
            .setHeight(genesisBlock.height)
            .setPreviousHash(genesisBlock.previousHash)
            .setMerkleHash(genesisBlock.merkleHash)
            .setTimestamp(genesisBlock.timestamp)
            .setEpochIndex(genesisBlock.epochIndex)
            .addAllActiveDelegateIps(genesisBlock.activeDelegateIps.toList())
            .build()
    }

}