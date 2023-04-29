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
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.security.PrivateKey

class DefaultBlockchainService(
    private val ownAddress: Address,
    private val privateKey: PrivateKey,
    private val networkService: NetworkService
) : BlockchainService {

    private val blocks = mutableMapOf<String, Block>()
    private val confirmedTransactions = mutableMapOf<String, Transaction>()

    /**
     * Unspent transactions in which this address is the recipient.
     */
    private val relevantTransactions = mutableMapOf<String, Transaction>()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val mutex = Mutex()

    private val miner = Miner(
        ownAddress = ownAddress,
        transactionProvider = object : TransactionProvider {
            override val transactions get() = confirmedTransactions
        },
        coroutineScope = coroutineScope
    )

    init {
        miner.onBlockMined = { block ->
            blocks[block.hash()] = block
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
            miner.startMining()
        }
    }

    private suspend fun downloadBlockchain() {
        networkService.sendRequest(
            request = GetBlockchainRequest,
            responseCount = 1,
            responseTimeout = 5000
        ) { response: GetBlockchainResponse ->
            response.blockchain.forEach { (blockHash, block) ->
                if (validateBlock(block)) {
                    blocks[blockHash] = block
                    confirmedTransactions += block.transactions.associateBy { it.id }
                }
            }
        }

        networkService.sendRequest(
            request = GetUnconfirmedTransactions,
            responseCount = 1,
            responseTimeout = 5000
        ) { response: GetUnconfirmedTransactionsResponse ->
            val unconfirmedTransactions = response.unconfirmedTransactions
            miner.includeTransactions(unconfirmedTransactions)
        }

        miner.setChainEnd(findLongestChainEnd())
    }

    private fun launchRequestListeners() {
        onRequest<GetBlockchainRequest> { request ->
            networkService.sendResponse(request, GetBlockchainResponse(blocks))
        }
        onRequest<GetUnconfirmedTransactions> { request ->
            val unconfirmedTransactions = miner.getUnconfirmedTransactions()
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
                    miner.setChainEnd(
                        if (block.prevHash == miner.currentBlock.prevHash) block
                        else findLongestChainEnd()!!
                    )

                    val leftoverTransactions =
                        miner.currentBlock.transactions.filterNot { block.transactions.contains(it) }
                    miner.setTransactions(leftoverTransactions)
                }
            }
        }
    }

    private fun findLongestChainEnd(): Block? {
        if (blocks.isEmpty()) return null
        val heights = hashMapOf<String, Int>()

        fun findHeight(block: Block): Int {
            return heights.getOrPut(block.hash()) {
                val prevBlock = blocks[block.prevHash]
                if (prevBlock != null) findHeight(prevBlock) + 1
                else 1
            }
        }

        blocks.values.forEach(::findHeight)

        val maxHeightHash = heights.maxBy { it.value }.key
        return blocks[maxHeightHash]
    }

    private suspend fun validateTransaction(
        transaction: Transaction,
        existingTransactions: Map<String, Transaction> = confirmedTransactions
    ): Boolean = mutex.withReentrantLock {
        // Coinbase transaction is validated at block level and always rejected here
        if (transaction.sender == null) return@withReentrantLock false

        val inputSum = transaction.inputs.sumOf { input ->
            val inputTransaction = existingTransactions[input.transactionId] ?: return@withReentrantLock false
            // We check that each input of this transaction isn't spent. It's spent if there is any existing transaction
            // which has it as an input.
            val spent = existingTransactions.values.any { existingTransaction ->
                existingTransaction.inputs.contains(input)
            }
            if (spent) return@withReentrantLock false

            return@sumOf inputTransaction.outputs.getOrNull(input.outputIndex)
                ?.takeIf { it.recipient == transaction.sender }
                ?.amount ?: return@withReentrantLock false
        }
        val outputSum = transaction.outputs.sumOf { it.amount }
        if (inputSum < outputSum) return@withReentrantLock false

        return@withReentrantLock transaction.senderSignature?.let {
            Crypto.verify(transaction.hash(), transaction.sender.publicKey, it)
        } ?: false
    }

    private suspend fun validateBlock(block: Block): Boolean = mutex.withReentrantLock {
        val prevBlock = blocks[block.prevHash] ?: return@withReentrantLock false

        if (block.transactions.isEmpty()) return@withReentrantLock false
        val validTimeRange = prevBlock.timestamp..System.currentTimeMillis()
        if (block.timestamp !in validTimeRange) return@withReentrantLock false
        if (!block.hash().fitsHashRequirement(miner.miningDifficulty)) return@withReentrantLock false
        if (!miner.validateCoinbaseTransaction(block)) return@withReentrantLock false

        val validTransactions = confirmedTransactions.toMutableMap()
        for (transaction in block.transactions) {
            if (validateTransaction(transaction, validTransactions)) {
                validTransactions[transaction.id] = transaction
            } else return@withReentrantLock false
        }

        return@withReentrantLock true
    }

    override suspend fun makeTransaction(
        amount: Int,
        recipient: Address,
        transactionFee: Int
    ): SendCoinsResult = mutex.withReentrantLock {
        val totalToSpend = amount + transactionFee

        val inputTransactions = mutableSetOf<Transaction>()
        var inputAmount = 0
        for (transaction in confirmedTransactions.values) {
            inputTransactions += transaction
            inputAmount += transaction.outputs.find { it.recipient == ownAddress }?.amount ?: 0

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
            sender = ownAddress,
            inputs = inputs,
            outputs = outputs
        ).apply { sign(privateKey) }

        coroutineScope.launch {
            networkService.broadcastMessage(transaction)
        }

        miner.includeTransaction(transaction)
        relevantTransactions.values.removeAll(inputTransactions)

        return@withReentrantLock SendCoinsResult.Success
    }
}