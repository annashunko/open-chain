package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.component.TransactionCapacityChecker
import io.openfuture.chain.core.exception.CoreException
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.INSUFFICIENT_ACTUAL_BALANCE
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.repository.TransferTransactionRepository
import io.openfuture.chain.core.repository.UTransferTransactionRepository
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.rpc.domain.base.PageRequest
import io.openfuture.chain.rpc.domain.transaction.request.TransferTransactionRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultTransferTransactionService(
    repository: TransferTransactionRepository,
    uRepository: UTransferTransactionRepository,
    capacityChecker: TransactionCapacityChecker
) : ExternalTransactionService<TransferTransaction, UnconfirmedTransferTransaction>(repository, uRepository, capacityChecker), TransferTransactionService {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DefaultTransferTransactionService::class.java)
    }


    @Transactional(readOnly = true)
    override fun getUnconfirmedCount(): Long {
        return unconfirmedRepository.count()
    }

    @Transactional(readOnly = true)
    override fun getByHash(hash: String): TransferTransaction = repository.findOneByFooterHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional(readOnly = true)
    override fun getAll(request: PageRequest): Page<TransferTransaction> = repository.findAll(request)

    @Transactional(readOnly = true)
    override fun getAllUnconfirmed(request: PageRequest): MutableList<UnconfirmedTransferTransaction> =
        unconfirmedRepository.findAllByOrderByHeaderFeeDesc(request)

    @Transactional(readOnly = true)
    override fun getUnconfirmedByHash(hash: String): UnconfirmedTransferTransaction =
        unconfirmedRepository.findOneByFooterHash(hash)
            ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional(readOnly = true)
    override fun getByAddress(address: String, request: PageRequest): Page<TransferTransaction> =
        (repository as TransferTransactionRepository).findAllByHeaderSenderAddressOrPayloadRecipientAddress(address, address, request)

    @BlockchainSynchronized
    @Synchronized
    @Transactional
    override fun add(message: TransferTransactionMessage) {
        try {
            super.add(UnconfirmedTransferTransaction.of(message))
        } catch (ex: CoreException) {
            log.debug(ex.message)
        }
    }

    @BlockchainSynchronized
    @Synchronized
    @Transactional
    override fun add(request: TransferTransactionRequest): UnconfirmedTransferTransaction {
        return super.add(UnconfirmedTransferTransaction.of(request))
    }

    @Transactional
    override fun toBlock(message: TransferTransactionMessage, block: MainBlock): TransferTransaction {
        val tx = repository.findOneByFooterHash(message.hash)
        if (null != tx) {
            return tx
        }

        walletService.increaseBalance(message.recipientAddress, message.amount)
        walletService.decreaseBalance(message.senderAddress, message.amount + message.fee)

        val utx = unconfirmedRepository.findOneByFooterHash(message.hash)
        if (null != utx) {
            walletService.decreaseUnconfirmedOutput(message.senderAddress, message.amount + message.fee)
            return confirm(utx, TransferTransaction.of(utx, block))
        }

        return this.save(TransferTransaction.of(message, block))
    }

    @Transactional
    override fun verify(message: TransferTransactionMessage): Boolean {
        return try {
            validate(UnconfirmedTransferTransaction.of(message))
            true
        } catch (e: ValidationException) {
            log.warn(e.message)
            false
        }
    }

    @Transactional
    override fun save(tx: TransferTransaction): TransferTransaction {
        return super.save(tx)
    }

    @Transactional
    override fun validateNew(utx: UnconfirmedTransferTransaction) {
        if (!isValidActualBalance(utx.header.senderAddress, utx.payload.amount + utx.header.fee)) {
            throw ValidationException("Insufficient actual balance", INSUFFICIENT_ACTUAL_BALANCE)
        }
    }

    @Transactional
    override fun validate(utx: UnconfirmedTransferTransaction) {
        if (utx.header.fee < 0) {
            throw ValidationException("Fee should not be less than 0")
        }

        if (utx.payload.amount <= 0) {
            throw ValidationException("Amount should not be less than or equal to 0")
        }

        super.validateExternal(utx.header, utx.payload, utx.footer)
    }

    @Transactional
    override fun updateUnconfirmedBalance(utx: UnconfirmedTransferTransaction) {
        super.updateUnconfirmedBalance(utx)
        walletService.increaseUnconfirmedOutput(utx.header.senderAddress, utx.payload.amount)
    }

}