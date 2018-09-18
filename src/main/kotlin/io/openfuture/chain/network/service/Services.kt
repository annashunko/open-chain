package io.openfuture.chain.network.service

import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.serialization.Serializable


interface NetworkApiService {

    fun broadcast(message: Serializable)

    fun isChannelsEmpty(): Boolean

    fun getConnectionSize(): Int

    fun sendRandom(message: Serializable)

    fun sendToAddress(message: Serializable, nodeInfo: NodeInfo)

    fun getNetworkSize(): Int

}

interface ConnectionService {

    fun connect(networkAddress: NetworkAddress, close: Boolean = false)

}