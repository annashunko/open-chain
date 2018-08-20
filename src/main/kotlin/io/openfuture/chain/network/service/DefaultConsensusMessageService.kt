package io.openfuture.chain.network.service

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.consensus.component.block.PendingBlockHandler
import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.network.message.consensus.BlockApprovalMessage
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import org.springframework.stereotype.Service

@Service
class DefaultConsensusMessageService(
    private val pendingBlockHandler: PendingBlockHandler
) : ConsensusMessageService {

    @BlockchainSynchronized
    override fun onBlockApproval(ctx: ChannelHandlerContext, block: BlockApprovalMessage) {
        pendingBlockHandler.handleApproveMessage(block)
    }

    @BlockchainSynchronized
    override fun onPendingBlock(ctx: ChannelHandlerContext, block: PendingBlockMessage) {
        pendingBlockHandler.addBlock(block)
    }

}