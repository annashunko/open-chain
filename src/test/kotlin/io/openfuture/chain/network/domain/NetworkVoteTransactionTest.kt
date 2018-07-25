package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NetworkVoteTransactionTest {

    private val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
        "00000000000000013ff00000000000003ff000000000000000000010726563697069656e74416464726573730000000973656e6465724b65" +
            "790000000d73656e646572416464726573730000000f73656e6465725369676e61747572650000000468617368000000010000000c64656c6567617465486f737400001f90"))
    private val entity = NetworkVoteTransaction(1, 1.0, 1.0, "recipientAddress", "senderKey", "senderAddress", "senderSignature", "hash",
        1, "delegateHost", 8080)

    @Test
    fun writeShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()

        entity.write(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBuffer() {
        val actualEntity = NetworkVoteTransaction::class.java.newInstance()

        actualEntity.read(buffer)

        assertThat(actualEntity.hash).isEqualTo(entity.hash)
    }

}