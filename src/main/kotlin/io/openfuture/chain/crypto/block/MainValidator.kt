package io.openfuture.chain.crypto.block

import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.BlockVersion
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.util.BlockUtils
import org.springframework.stereotype.Component

@Component
class MainValidator : Validator {

    override fun isValid(block: Block): Boolean {
        val mainBlock = block as MainBlock
        val transactions = mainBlock.transactions

        if (transactions.isEmpty()) {
            return false
        }

        if (!transactionsIsWellFormed(transactions)) {
            return false
        }

        val transactionsMerkleHash = BlockUtils.calculateMerkleRoot(transactions)
        if (block.merkleHash != transactionsMerkleHash) {
            return false
        }
        return true
    }

    override fun getVersion(): Int {
        return BlockVersion.MAIN.version
    }

    private fun transactionsIsWellFormed(transactions: Set<Transaction>): Boolean {
        val transactionHashes = HashSet<String>()
        for (transaction in transactions) {

            val transactionHash = transaction.hash
            if (transactionHashes.contains(transactionHash)) {
                return false
            }

            transactionHashes.add(transactionHash)
        }

        return true
    }

}