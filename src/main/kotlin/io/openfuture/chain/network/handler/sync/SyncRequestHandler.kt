package io.openfuture.chain.network.handler.sync

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.network.message.sync.SyncRequestMessage
import io.openfuture.chain.network.message.sync.SyncResponseMessage
import org.springframework.stereotype.Component

@Component
@Sharable
class SyncRequestHandler(
    private val genesisBlockService: GenesisBlockService
) : SimpleChannelInboundHandler<SyncRequestMessage>() {


    override fun channelRead0(ctx: ChannelHandlerContext, msg: SyncRequestMessage) {
        val lastGenesisBlock = genesisBlockService.getLast()
        ctx.writeAndFlush(SyncResponseMessage(lastGenesisBlock.toMessage()))
    }

}