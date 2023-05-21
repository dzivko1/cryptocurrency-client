package com.github.dzivko1.dullcoin.data.blockchain

import com.github.dzivko1.dullcoin.data.util.fitsHashRequirement
import com.github.dzivko1.dullcoin.domain.blockchain.model.Address
import com.github.dzivko1.dullcoin.domain.blockchain.model.Block
import com.github.dzivko1.dullcoin.domain.blockchain.model.Transaction
import kotlin.concurrent.thread

class Miner(
    private val ownAddress: Address,
    private val transactionProvider: TransactionProvider
) {
    var currentBlock: Block = createBlock(prevBlockHash = "")
        private set

    private var newBlock: Block? = null

    /**
     * Transactions that are not yet included in a block.
     */
    private val queuedTransactions = linkedMapOf<String, Transaction>()

    var miningDifficulty = 4

    var minedBlockHeight = 0
        private set

    var onBlockMined: ((Block) -> Unit)? = null

    private var miningThread: Thread? = null

    fun getTransactions(): List<Transaction> {
        return currentBlock.transactions.drop(1) + queuedTransactions.values
    }

    fun setChainEnd(block: Block?, blockHeight: Int, newBlockTransactions: List<Transaction>) = synchronized(this) {
        minedBlockHeight = blockHeight + 1
        newBlock = createBlock(
            prevBlockHash = block?.hash() ?: "",
            initialTransactions = newBlockTransactions
        )
    }

    fun includeTransaction(transaction: Transaction) = synchronized(this) {
        queuedTransactions[transaction.id] = transaction
    }

    fun includeTransactions(transactions: List<Transaction>) = synchronized(this) {
        queuedTransactions += transactions.associateBy { it.id }
    }

    fun startMining() {
        miningThread = thread {
            mine()
        }
    }

    fun stopMining() {
        miningThread?.interrupt()
        miningThread?.join()
        miningThread = null
    }

    private fun mine() {
        while (!Thread.interrupted()) {
            newBlock?.let {
                currentBlock = it
                newBlock = null
            }

            val existingBlockTransactions = currentBlock.transactions.drop(1)
            val blockTransactions = existingBlockTransactions + queuedTransactions.values.toList()

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
            val timeout = System.currentTimeMillis() + 1000
            currentBlock.timestamp = System.currentTimeMillis()
            var hash = currentBlock.hash()
            while (!hash.fitsHashRequirement(miningDifficulty)) {
                currentBlock.nonce++
                if (System.currentTimeMillis() > timeout || newBlock != null) break
                hash = currentBlock.hash()
            }

            if (hash.fitsHashRequirement(miningDifficulty)) {
                synchronized(this) {
                    onBlockMined?.invoke(currentBlock)
                    minedBlockHeight++
                    currentBlock = createBlock(prevBlockHash = hash)
                }
            }
        }
    }

    private fun createBlock(prevBlockHash: String, initialTransactions: List<Transaction> = emptyList()): Block {
        return Block(
            prevHash = prevBlockHash,
            initialTransactions = buildList {
                add(createCoinbaseTransaction(blockTransactions = initialTransactions))
                addAll(initialTransactions)
            }
        )
    }

    private fun createCoinbaseTransaction(blockTransactions: List<Transaction>): Transaction = synchronized(this) {
        val fees = calculateFees(blockTransactions)
        return Transaction(
            senderPublicKey = null,
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

        if (firstTransaction.senderPublicKey != null ||
            firstTransaction.inputs.isNotEmpty() ||
            firstTransaction.outputs.size != 1
        ) return false

        val fees = calculateFees(block.transactions.drop(1))

        return firstTransaction.outputs.first().amount <= calculateBlockReward(blockHeight) + fees
    }

    private fun calculateFees(transactions: List<Transaction>): Long = synchronized(this) {
        var totalInputs = 0L
        var totalOutputs = 0L
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