package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.BaseTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.TransactionService
import io.openfuture.chain.core.service.WalletService
import io.openfuture.chain.core.util.TransactionUtils
import io.openfuture.chain.crypto.service.CryptoService
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.component.node.NodeClock
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.beans.factory.annotation.Autowired
import javax.xml.bind.ValidationException

abstract class BaseTransactionService<T : Transaction, U: UnconfirmedTransaction> (
    protected val repository: TransactionRepository<T>,
    protected val uRepository: UTransactionRepository<U>
) {

    @Autowired
    protected lateinit var baseService: TransactionService

    @Autowired
    protected lateinit var clock: NodeClock

    @Autowired
    protected lateinit var walletService: WalletService

    @Autowired
    private lateinit var cryptoService: CryptoService

    protected fun save(utx: U) : U {
        if (!isValid(utx)) {
            throw ValidationException("Transaction is invalid!")
        }
        return uRepository.save(utx)
    }

    protected fun save(tx: T) : T {
        if (!isValid(tx)) {
            throw ValidationException("Transaction is invalid!")
        }
        updateBalanceByFee(tx)
        return repository.save(tx)
    }

    protected fun toBlock(utx: U, tx: T, block: MainBlock): T {
        updateBalanceByFee(utx)
        uRepository.delete(utx)
        return repository.save(tx.apply { tx.block = block })
    }

    open fun isValid(utx: U): Boolean {
        return this.isValidBase(utx)
    }

    open fun isValid(tx: T): Boolean {
        return this.isValidBase(tx)
    }

    private fun updateBalanceByFee(tx: BaseTransaction) {
        walletService.decreaseBalance(tx.senderAddress, tx.fee)
    }

    private fun isValidBase(tx: BaseTransaction): Boolean {
        return isValidAddress(tx.senderAddress, tx.senderPublicKey)
            && isValidFee(tx.senderAddress, tx.fee)
            && isValidHash(tx.timestamp, tx.fee, tx.senderAddress, tx.getPayload(), tx.hash)
            && isValidaSignature(tx.hash, tx.senderSignature, tx.senderPublicKey)
    }

    private fun isValidAddress(senderAddress: String, senderPublicKey: String): Boolean {
        return !cryptoService.isValidAddress(senderAddress, ByteUtils.fromHexString(senderPublicKey))
    }

    private fun isValidFee(senderAddress: String, fee: Long): Boolean {
        val balance = walletService.getBalanceByAddress(senderAddress)
        val unspentBalance = balance - baseService.getAllUnconfirmedByAddress(senderAddress).map { it.fee }.sum()

        if (unspentBalance < fee) {
            return false
        }
        return true
    }

    private fun isValidHash(timestamp: Long, fee: Long, senderAddress: String, payload: TransactionPayload, hash: String): Boolean {
        return TransactionUtils.generateHash(timestamp, fee, senderAddress, payload) == hash
    }

    private fun isValidaSignature(hash: String, signature: String, publicKey: String): Boolean {
        return SignatureUtils.verify(ByteUtils.fromHexString(hash), signature, ByteUtils.fromHexString(publicKey))
    }

}