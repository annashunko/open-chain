package io.openfuture.chain.network.component

import io.netty.channel.Channel
import io.netty.channel.ChannelId
import io.netty.channel.group.DefaultChannelGroup
import io.netty.util.AttributeKey
import io.netty.util.concurrent.GlobalEventExecutor
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.exception.NotFoundChannelException
import io.openfuture.chain.network.serialization.Serializable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ChannelsHolder {

    companion object {
        val NODE_INFO_KEY = AttributeKey.valueOf<NodeInfo>("uid")
        private val log: Logger = LoggerFactory.getLogger(ChannelsHolder::class.java)
    }

    private val channelGroup = DefaultChannelGroup(GlobalEventExecutor.INSTANCE)


    fun broadcast(message: Serializable) {
        channelGroup.writeAndFlush(message)
    }

    fun sendRandom(message: Serializable) {
        val channel = channelGroup.shuffled().firstOrNull()
            ?: throw NotFoundChannelException("List channels is empty")
        channel.writeAndFlush(message)
    }

    fun send(message: Serializable, nodeInfo: NodeInfo): Boolean {
        val channel = channelGroup.firstOrNull { it.attr(NODE_INFO_KEY).get() == nodeInfo } ?: return false
        channel.writeAndFlush(message)
        return true
    }

    fun remove(channel: Channel) {
        channelGroup.remove(channel)
    }

    fun size(): Int = channelGroup.size

    fun isEmpty(): Boolean = channelGroup.isEmpty()

    fun getNodesInfo(): List<NodeInfo> = channelGroup.map { it.attr(NODE_INFO_KEY).get() }

    fun getNodeInfoByChannelId(channelId: ChannelId): NodeInfo? = channelGroup.find(channelId)?.attr(NODE_INFO_KEY)?.get()

    @Synchronized
    fun addChannel(channel: Channel, nodeInfo: NodeInfo): Boolean {
        if(channelGroup.any { it.attr(NODE_INFO_KEY).get() == nodeInfo }) {
            return false
        }
        channel.attr(NODE_INFO_KEY).setIfAbsent(nodeInfo)
        log.debug("${channel.remoteAddress()} connected, operating peers count is ${size()}")
        return channelGroup.add(channel)
    }

    @Synchronized
    fun hasChannel(channel: Channel): Boolean = channelGroup.contains(channel)

}