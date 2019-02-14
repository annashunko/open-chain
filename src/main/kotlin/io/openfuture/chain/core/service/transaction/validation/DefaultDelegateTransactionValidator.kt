package io.openfuture.chain.core.service.transaction.validation

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.ALREADY_DELEGATE
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.repository.UDelegateTransactionRepository
import io.openfuture.chain.core.service.DelegateTransactionValidator
import io.openfuture.chain.core.service.StateManager
import io.openfuture.chain.core.sync.BlockchainLock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultDelegateTransactionValidator(
    private val consensusProperties: ConsensusProperties,
    private val stateManager: StateManager,
    private val uRepository: UDelegateTransactionRepository
) : DelegateTransactionValidator {

    override fun validate(tx: DelegateTransaction, new: Boolean) {
        checkFeeDelegateTx(tx)
        checkAmountDelegateTx(tx)
        if (new) {
            checkDelegate(tx)
            checkSendRequest(tx)
        }
    }

    private fun checkFeeDelegateTx(tx: DelegateTransaction) {
        if (tx.fee != consensusProperties.feeDelegateTx!!) {
            throw ValidationException("Fee should be ${consensusProperties.feeDelegateTx!!}")
        }
    }

    private fun checkAmountDelegateTx(tx: DelegateTransaction) {
        if (tx.getPayload().amount != consensusProperties.amountDelegateTx!!) {
            throw ValidationException("Amount should be ${consensusProperties.amountDelegateTx!!}")
        }
    }

    private fun checkDelegate(tx: DelegateTransaction) {
        if (stateManager.isExistsDelegateByPublicKey(tx.getPayload().delegateKey)) {
            throw ValidationException("Node ${tx.getPayload().delegateKey} already registered as delegate",
                ALREADY_DELEGATE)
        }
    }

    private fun checkSendRequest(tx: DelegateTransaction) {
        BlockchainLock.readLock.lock()
        try {
            if (uRepository.findAll().any { it.getPayload().delegateKey == tx.getPayload().delegateKey }) {
                throw ValidationException("Node ${tx.getPayload().delegateKey} already send request to become delegate",
                    ALREADY_DELEGATE)
            }
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

}