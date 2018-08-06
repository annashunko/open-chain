package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.service.CommonBlockService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
import org.springframework.stereotype.Component
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
class BlockProductionScheduler(
    private val keyHolder: NodeKeyHolder,
    private val epochService: EpochService,
    private val commonBlockService: CommonBlockService,
    private val mainBlockService: MainBlockService,
    private val genesisBlockService: GenesisBlockService,
    private val consensusProperties: ConsensusProperties,
    private val pendingBlockHandler: PendingBlockHandler
) {

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    private var currentTimeSlot: Long = 0


    @PostConstruct
    fun init() {
        executor.submit {
            while (true) {
                val timeSlot = epochService.getSlotNumber()
                if (timeSlot > currentTimeSlot) {
                    currentTimeSlot = timeSlot
                    val slotOwner = epochService.getCurrentSlotOwner()
                    if (isGenesisBlockRequired()) {
                        // create genesis block
                        // epochService.switchEpoch()
                    } else if (keyHolder.getPublicKey() == slotOwner.publicKey) {
                        // create main block
                        // pendingBlockHandler.addBlock()
                    }
                }
                Thread.sleep(100)
            }
        }
    }

    @PreDestroy
    fun shutdown() {
        executor.shutdown()
    }

    private fun isGenesisBlockRequired(): Boolean {
        val blocksProduced = commonBlockService.getLast().height - epochService.getGenesisBlockHeight()
        return (consensusProperties.epochHeight!! - 1) <= blocksProduced
    }

}