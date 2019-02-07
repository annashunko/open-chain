package io.openfuture.chain.core.service.state

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.component.StatePool
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.repository.DelegateStateRepository
import io.openfuture.chain.core.service.DelegateStateService
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.springframework.data.domain.Sort.Direction.DESC
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultDelegateStateService(
    private val repository: DelegateStateRepository,
    private val consensusProperties: ConsensusProperties,
    private val statePool: StatePool
) : DelegateStateService {

    override fun getAllDelegates(request: PageRequest): List<DelegateState> = repository.findLastAll(request)

    override fun getActiveDelegates(): List<DelegateState> {
        val sortBy = setOf("rating", "id")
        return getAllDelegates(PageRequest(0, consensusProperties.delegatesCount!!, sortBy, DESC))
    }

    override fun isExistsByPublicKey(key: String): Boolean = null != repository.findFirstByAddressOrderByBlockIdDesc(key)

    override fun isExistsByPublicKeys(publicKeys: List<String>): Boolean = publicKeys.all { isExistsByPublicKey(it) }

    override fun updateRating(delegateKey: String, amount: Long): DelegateState {
        val state = getCurrentState(delegateKey)

        val newState = DelegateState(state.address, state.walletAddress, state.createDate, state.rating + amount)
        statePool.update(newState)
        return newState
    }

    override fun addDelegate(delegateKey: String, walletAddress: String, createDate: Long): DelegateState {
        val newState = DelegateState(delegateKey, walletAddress, createDate)
        statePool.update(newState)
        return newState
    }

    private fun getCurrentState(address: String): DelegateState {
        BlockchainLock.readLock.lock()
        try {
            return statePool.get(address) as? DelegateState
                ?: repository.findFirstByAddressOrderByBlockIdDesc(address)!!
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

}