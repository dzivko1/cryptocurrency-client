package com.github.dzivko1.dullcoin.data.blockchain

import com.github.dzivko1.dullcoin.data.util.fitsHashRequirement
import com.github.dzivko1.dullcoin.domain.blockchain.model.Address
import com.github.dzivko1.dullcoin.domain.blockchain.model.Block
import com.github.dzivko1.dullcoin.domain.blockchain.model.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Miner(
    private val ownAddress: Address,
    private val transactionProvider: TransactionProvider,
    private val coroutineScope: CoroutineScope
) {
    var currentBlock: Block = Block(prevHash = "")
        private set

    /**
     * Transactions that are not yet included in a block.
     */
    private val queuedTransactions = linkedMapOf<String, Transaction>()

    var miningDifficulty = 3

    var minedBlockHeight = 0
        private set

    var onBlockMined: ((Block) -> Unit)? = null

    fun getUnconfirmedTransactions(): List<Transaction> {
        return currentBlock.transactions + queuedTransactions.values
    }

    fun setChainEnd(block: Block?, blockHeight: Int) = synchronized(this) {
        currentBlock = Block(
            prevHash = block?.hash() ?: "",
            initialTransactions = currentBlock.transactions
        )
        minedBlockHeight = blockHeight + 1
    }

    fun includeTransaction(transaction: Transaction) = synchronized(this) {
        queuedTransactions[transaction.id] = transaction
    }

    fun includeTransactions(transactions: List<Transaction>) = synchronized(this) {
        queuedTransactions += transactions.associateBy { it.id }
    }

    fun setTransactions(transactions: List<Transaction>) = synchronized(this) {
        queuedTransactions.clear()
        includeTransactions(transactions)
    }

    fun startMining() = coroutineScope.launch {
        while (true) {
            val existingBlockTransactions = currentBlock.transactions.drop(1)
            val blockTransactions = existingBlockTransactions + queuedTransactions.values.toList()

            if (blockTransactions.isEmpty()) {
                // Nothing to mine, wait and check again
                delay(1000)
                continue
            }

            if (existingBlockTransactions != blockTransactions) {
                // Transaction pool changed, update block and recalculate coinbase transaction
                synchronized(this) {
                    currentBlock.clearTransactions()
                    currentBlock.addTransaction(createCoinbaseTransaction(blockTransactions))
                    currentBlock.addTransactions(blockTransactions)
                    queuedTransactions.clear()
                }
            }

            // Mine for some time before refreshing the block
            val timeout = System.currentTimeMillis() + 5000
            var hash = currentBlock.hash()
            while (!hash.fitsHashRequirement(miningDifficulty)) {
                currentBlock.nonce++
                if (System.currentTimeMillis() > timeout) break
                hash = currentBlock.hash()
            }

            if (hash.fitsHashRequirement(miningDifficulty)) {
                synchronized(this) {
                    onBlockMined?.invoke(currentBlock)
                    currentBlock = Block(hash)
                    minedBlockHeight++
                }
            }
        }
    }

    private fun createCoinbaseTransaction(blockTransactions: List<Transaction>): Transaction {
        val fees = calculateFees(blockTransactions)
        return Transaction(
            sender = null,
            inputs = emptyList(),
            outputs = listOf(
                Transaction.Output(
                    amount = calculateBlockReward(minedBlockHeight) + fees,
                    recipient = ownAddress
                )
            )
        )
    }

    fun validateCoinbaseTransaction(
        block: Block,
        blockHeight: Int
    ): Boolean = synchronized(this) {
        val firstTransaction = block.transactions.firstOrNull() ?: return false

        if (firstTransaction.sender != null ||
            firstTransaction.inputs.isNotEmpty() ||
            firstTransaction.outputs.size != 1
        ) return false

        val fees = calculateFees(block.transactions)

        return firstTransaction.outputs.first().amount <= calculateBlockReward(blockHeight) + fees
    }

    private fun calculateFees(transactions: List<Transaction>): Int {
        var totalInputs = 0
        var totalOutputs = 0
        transactions.forEach { transaction ->
            totalInputs += transaction.inputs.sumOf { input ->
                val inputTransaction = transactionProvider.transactions[input.transactionId]
                inputTransaction?.outputs?.getOrNull(input.outputIndex)?.amount ?: 0
            }
            totalOutputs += transaction.outputs.sumOf { it.amount }
        }
        return totalInputs - totalOutputs
    }

    private fun calculateBlockReward(blockHeight: Int): Int {
        return (100_000_000 - blockHeight).coerceAtLeast(0)
    }
}