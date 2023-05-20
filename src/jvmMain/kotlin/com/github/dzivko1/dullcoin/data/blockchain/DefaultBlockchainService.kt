package com.github.dzivko1.dullcoin.data.blockchain

import com.github.dzivko1.dullcoin.crypto.Crypto
import com.github.dzivko1.dullcoin.data.blockchain.model.GetBlockchainRequest
import com.github.dzivko1.dullcoin.data.blockchain.model.GetBlockchainResponse
import com.github.dzivko1.dullcoin.data.blockchain.model.GetUnconfirmedTransactions
import com.github.dzivko1.dullcoin.data.blockchain.model.GetUnconfirmedTransactionsResponse
import com.github.dzivko1.dullcoin.data.core.network.*
import com.github.dzivko1.dullcoin.data.util.fitsHashRequirement
import com.github.dzivko1.dullcoin.domain.blockchain.BlockchainService
import com.github.dzivko1.dullcoin.domain.blockchain.model.Address
import com.github.dzivko1.dullcoin.domain.blockchain.model.Block
import com.github.dzivko1.dullcoin.domain.blockchain.model.Transaction
import com.github.dzivko1.dullcoin.domain.blockchain.usecase.SendCoinsResult
import com.github.dzivko1.dullcoin.util.withReentrantLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.security.PrivateKey
import java.security.PublicKey
import kotlin.math.pow

class DefaultBlockchainService(
    private val ownAddress: Address,
    private val ownPublicKey: PublicKey,
    private val privateKey: PrivateKey,
    private val networkService: NetworkService
) : BlockchainService {

    private val blocks = linkedMapOf<String, Block>()
    private val confirmedTransactions = linkedMapOf<String, Transaction>()

    /**
     * Unspent transactions in which this address is the recipient.
     */
    private val relevantTransactions = linkedMapOf<String, Transaction>()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val mutex = Mutex()

    private val miner = Miner(
        ownAddress = ownAddress,
        transactionProvider = object : TransactionProvider {
            override val transactions get() = confirmedTransactions
        }
    )

    private val _balanceFlow = MutableStateFlow(0L)
    override val balanceFlow: Flow<Long>
        get() = _balanceFlow.asStateFlow()

    init {
        miner.onBlockMined = { block ->
            val blockTransactions = block.transactions.associateBy { it.id }
            confirmedTransactions.putAll(blockTransactions)
            relevantTransactions.putAll(
                blockTransactions.filterValues { transaction ->
                    transaction.outputs.any { it.recipient == ownAddress }
                }
            )
            blocks[block.hash()] = block
            updateBalance()
            miner.miningDifficulty = calculateMiningDifficulty()
            coroutineScope.launch {
                networkService.broadcastMessage(block)
            }
        }
    }

    override fun connectToNetwork() {
        networkService.connect()
    }

    override fun disconnectFromNetwork() {
        networkService.disconnect()
    }

    override fun startBlockchainMaintenance() {
        coroutineScope.launch {
            downloadBlockchain()
            launchRequestListeners()
            launch { listenForTransactions() }
            launch { listenForBlocks() }
            miner.miningDifficulty = calculateMiningDifficulty()
            miner.startMining()
        }
    }

    private suspend fun downloadBlockchain() {
        networkService.sendRequest(
            request = GetBlockchainRequest,
            responseCount = 1,
            responseTimeout = 50
        ) { response: GetBlockchainResponse ->
            response.blockchain.forEach { (blockHash, block) ->
                if (validateBlock(block)) {
                    blocks[blockHash] = block
                    confirmedTransactions += block.transactions.associateBy { it.id }
                    relevantTransactions += confirmedTransactions.filterValues { transaction ->
                        transaction.outputs.any { it.recipient == ownAddress }
                    }
                    updateBalance()
                }
            }
        }

        networkService.sendRequest(
            request = GetUnconfirmedTransactions,
            responseCount = 1,
            responseTimeout = 50
        ) { response: GetUnconfirmedTransactionsResponse ->
            val unconfirmedTransactions = response.unconfirmedTransactions
            miner.includeTransactions(unconfirmedTransactions)
        }

        val (longestChainEnd, height) = findLongestChainEnd()
        miner.setChainEnd(
            block = longestChainEnd,
            blockHeight = height,
            newBlockTransactions = miner.getTransactions()
        )
    }

    private fun launchRequestListeners() {
        onRequest<GetBlockchainRequest> { request ->
            networkService.sendResponse(request, GetBlockchainResponse(blocks))
        }
        onRequest<GetUnconfirmedTransactions> { request ->
            val unconfirmedTransactions = miner.getTransactions()
            networkService.sendResponse(request, GetUnconfirmedTransactionsResponse(unconfirmedTransactions))
        }
    }

    private inline fun <reified T> onRequest(crossinline onRequest: suspend (Request<T>) -> Unit) {
        coroutineScope.launch {
            networkService.getRequestFlow<T>().collect { request ->
                mutex.withReentrantLock {
                    onRequest(request)
                }
            }
        }
    }

    private suspend fun listenForTransactions() {
        networkService.getMessageFlow<Transaction>().collect { transaction ->
            mutex.withReentrantLock {
                if (validateTransaction(transaction)) {
                    miner.includeTransaction(transaction)
                }
            }
        }
    }

    private suspend fun listenForBlocks() {
        networkService.getMessageFlow<Block>().collect { block ->
            mutex.withReentrantLock {
                if (validateBlock(block)) {
                    val blockTransactions = block.transactions.associateBy { it.id }
                    confirmedTransactions.putAll(blockTransactions)
                    relevantTransactions.putAll(
                        blockTransactions.filterValues { transaction ->
                            transaction.outputs.any { it.recipient == ownAddress }
                        }
                    )
                    blocks[block.hash()] = block
                    updateBalance()

                    miner.miningDifficulty = calculateMiningDifficulty()

                    val heightToPass = if (block.prevHash == miner.currentBlock.prevHash) {
                        // The received block was in the place of our mined block
                        miner.minedBlockHeight
                    } else {
                        // The received block was somewhere else, check if it made a new longest chain or not
                        findBlockHeight(block).takeIf { it >= miner.minedBlockHeight }
                    }

                    // Relocate to the new longest chain if one was made
                    if (heightToPass != null) {
                        val leftoverTransactions = miner.currentBlock.transactions
                            .drop(1)
                            .filterNot { block.transactions.contains(it) }
                        miner.setChainEnd(
                            block = block,
                            blockHeight = heightToPass,
                            newBlockTransactions = leftoverTransactions
                        )
                    }
                }
            }
        }
    }

    private suspend fun validateTransaction(
        transaction: Transaction,
        existingTransactions: Map<String, Transaction> = confirmedTransactions
    ): Boolean = mutex.withReentrantLock {
        // Coinbase transaction is validated at block level and always rejected here
        if (transaction.senderPublicKey == null) return@withReentrantLock false

        val inputSum = transaction.inputs.sumOf { input ->
            val inputTransaction = existingTransactions[input.transactionId] ?: return@withReentrantLock false
            // We check that each input of this transaction isn't spent. It's spent if there is any existing transaction
            // which has it as an input.
            val spent = existingTransactions.values.any { existingTransaction ->
                existingTransaction.inputs.contains(input)
            }
            if (spent) return@withReentrantLock false

            val sender = Address(transaction.senderPublicKey)
            return@sumOf inputTransaction.outputs.getOrNull(input.outputIndex)
                ?.takeIf { it.recipient == sender }
                ?.amount ?: return@withReentrantLock false
        }
        val outputSum = transaction.outputs.sumOf { it.amount }
        if (inputSum < outputSum) return@withReentrantLock false

        return@withReentrantLock transaction.senderSignature?.let {
            Crypto.verify(transaction.hash(), transaction.senderPublicKey, it)
        } ?: false
    }

    private suspend fun validateBlock(block: Block): Boolean = mutex.withReentrantLock {
        val prevBlock = blocks[block.prevHash]
        if (prevBlock == null && block.prevHash != "") return@withReentrantLock false

        val validTimeRange = (prevBlock?.timestamp ?: 0)..System.currentTimeMillis()
        if (block.timestamp !in validTimeRange) return@withReentrantLock false
        if (!block.hash().fitsHashRequirement(miner.miningDifficulty)) return@withReentrantLock false
        if (!miner.validateCoinbaseTransaction(block, findBlockHeight(block))) return@withReentrantLock false

        val validTransactions = confirmedTransactions.toMutableMap()
        for (transaction in block.transactions.drop(1)) {
            if (validateTransaction(transaction, validTransactions)) {
                validTransactions[transaction.id] = transaction
            } else return@withReentrantLock false
        }

        return@withReentrantLock true
    }

    override fun getUserTransactions(): List<Transaction> {
        return confirmedTransactions.values.filter { transaction ->
            transaction.senderPublicKey == ownPublicKey ||
                    transaction.outputs.any { it.recipient == ownAddress }
        }
    }

    override suspend fun makeTransaction(
        amount: Long,
        recipient: Address,
        transactionFee: Long
    ): SendCoinsResult = mutex.withReentrantLock {
        val totalToSpend = amount + transactionFee

        val inputTransactions = mutableSetOf<Transaction>()
        var inputAmount = 0L
        for (transaction in relevantTransactions.values) {
            inputTransactions += transaction
            inputAmount += transaction.outputs.find { it.recipient == ownAddress }?.amount ?: 0L

            if (inputAmount >= totalToSpend) break
        }
        if (inputAmount < totalToSpend) return@withReentrantLock SendCoinsResult.InsufficientFunds

        val changeAmount = inputAmount - totalToSpend
        val inputs = inputTransactions.map { transaction ->
            Transaction.Input(
                transactionId = transaction.id,
                outputIndex = transaction.outputs.indexOfFirst { it.recipient == ownAddress }
            )
        }
        val outputs = listOf(
            Transaction.Output(amount, recipient),
            Transaction.Output(changeAmount, ownAddress)
        )
        val transaction = Transaction(
            senderPublicKey = ownPublicKey,
            inputs = inputs,
            outputs = outputs
        ).apply { sign(privateKey) }

        coroutineScope.launch {
            networkService.broadcastMessage(transaction)
        }

        miner.includeTransaction(transaction)
        relevantTransactions.values.removeAll(inputTransactions)
        updateBalance()

        return@withReentrantLock SendCoinsResult.Success
    }

    private fun updateBalance() {
        val balance = relevantTransactions.values.sumOf { transaction ->
            transaction.outputs
                .filter { it.recipient == ownAddress }
                .sumOf { it.amount }
        }
        _balanceFlow.value = balance
    }

    /**
     * @return A Pair where the first element is the ending block of the longest chain, and the second element is its height.
     */
    private fun findLongestChainEnd(): Pair<Block?, Int> {
        if (blocks.isEmpty()) return Pair(null, -1)
        val heights = hashMapOf<String, Int>()

        fun findHeight(block: Block): Int {
            return heights.getOrPut(block.hash()) {
                val prevBlock = blocks[block.prevHash]
                if (prevBlock != null) findHeight(prevBlock) + 1
                else 1
            }
        }

        blocks.values.forEach(::findHeight)

        val (maxHeightHash, maxHeight) = heights.maxBy { it.value }
        return Pair(blocks[maxHeightHash], maxHeight)
    }

    private fun findBlockHeight(block: Block): Int {
        var height = 0
        var b = blocks[block.prevHash]
        while (b != null) {
            height++
            b = blocks[b.prevHash]
        }
        return height
    }

    private fun calculateMiningDifficulty(): Int {
        val windowSize = 10
        val targetBlockTime = 30_000L

        if (blocks.size < windowSize) return miner.miningDifficulty

        val timestamps = blocks.values.toList().takeLast(windowSize).map { it.timestamp }
        val averageBlockTime = (timestamps.last() - timestamps.first()) / windowSize.toDouble()
        val timeRatio = targetBlockTime / averageBlockTime
        return (miner.miningDifficulty * timeRatio.pow(0.1)).toInt()
    }
}