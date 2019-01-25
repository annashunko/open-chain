package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.core.sync.ChainSynchronizer
import io.openfuture.chain.core.sync.SyncStatus.SYNCHRONIZED
import io.openfuture.chain.network.component.time.ClockChecker
import io.openfuture.chain.network.component.time.ClockSyncStatus.NOT_SYNCHRONIZED
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Component
class BlockProductionScheduler(
    private val keyHolder: NodeKeyHolder,
    private val epochService: EpochService,
    private val mainBlockService: MainBlockService,
    private val genesisBlockService: GenesisBlockService,
    private val pendingBlockHandler: PendingBlockHandler,
    private val chainSynchronizer: ChainSynchronizer,
    private val clockChecker: ClockChecker
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(BlockProductionScheduler::class.java)
    }

    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()


    @PostConstruct
    fun init() {
        executor.schedule({ proceedProductionLoop() }, epochService.timeToNextTimeSlot(), TimeUnit.MILLISECONDS)
    }

    private fun proceedProductionLoop() {
        try {
            if (NOT_SYNCHRONIZED == clockChecker.getStatus()) {
                log.error("Please set up Time synchronization by the ntp servers")
                return
            }

            if (SYNCHRONIZED != chainSynchronizer.getStatus()) {
                log.debug("Ledger is ${chainSynchronizer.getStatus()}")
                chainSynchronizer.checkLastBlock()
                return
            }

            val slotOwner = epochService.getCurrentSlotOwner()
            log.debug("CONSENSUS: Slot owner $slotOwner")
            if (genesisBlockService.isGenesisBlockRequired()) {
                val genesisBlock = genesisBlockService.create()
                genesisBlockService.add(genesisBlock)
                log.debug("CONSENSUS: Saving genesis block with hash = ${genesisBlock.hash}")
                pendingBlockHandler.resetSlotNumber()
            } else if (keyHolder.getPublicKeyAsHexString() == slotOwner) {
                val block = mainBlockService.create()
                pendingBlockHandler.addBlock(block)
            }
        } catch (ex: Exception) {
            log.error("Block creation failure inbound: ${ex.message}")
        } finally {
            executor.schedule({ proceedProductionLoop() }, epochService.timeToNextTimeSlot(), TimeUnit.MILLISECONDS)
        }
    }

}