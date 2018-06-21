package io.openfuture.chain.nio.server.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.nio.base.BaseHandler
import io.openfuture.chain.protocol.CommunicationProtocol.*
import io.openfuture.chain.component.NodeClock
import io.openfuture.chain.protocol.CommunicationProtocol.Type.*
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class TimeSyncServerHandler(
        private val clock: NodeClock
) : BaseHandler(TIME_SYNC_REQUEST) {

    override fun packetReceived(ctx: ChannelHandlerContext, message: Packet) {
        val response = Packet.newBuilder()
                .setType(TIME_SYNC_RESPONSE)
                .setTimeSyncResponse(TimeSyncResponse.newBuilder()
                        .setNetworkTimestamp(clock.networkTime())
                        .setNodeTimestamp(message.timeSyncRequest.nodeTimestamp)
                        .build())
                .build()
        ctx.channel().writeAndFlush(response)
    }
}