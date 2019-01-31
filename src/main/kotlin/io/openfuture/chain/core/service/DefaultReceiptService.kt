package io.openfuture.chain.core.service

import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.repository.ReceiptRepository
import io.openfuture.chain.core.sync.BlockchainLock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultReceiptService(
    private val repository: ReceiptRepository
) : ReceiptService {

    @Transactional(readOnly = true)
    override fun getByTransactionHash(hash: String): Receipt = repository.findOneByTransactionHash(hash)
        ?: throw NotFoundException("Receipt for transaction $hash not found")

    @Transactional
    override fun commit(receipt: Receipt) {
        BlockchainLock.writeLock.lock()
        try {
            repository.save(receipt)
        } finally {
            BlockchainLock.writeLock.unlock()
        }
    }

}