package com.github.dzivko1.dullcoin.data.blockchain

import com.github.dzivko1.dullcoin.crypto.Crypto
import com.github.dzivko1.dullcoin.data.blockchain.model.GetBlockchainRequest
import com.github.dzivko1.dullcoin.data.blockchain.model.GetBlockchainResponse
import com.github.dzivko1.dullcoin.data.core.network.*
import com.github.dzivko1.dullcoin.domain.blockchain.BlockchainService
import com.github.dzivko1.dullcoin.domain.blockchain.model.Address
import com.github.dzivko1.dullcoin.domain.blockchain.model.Block
import com.github.dzivko1.dullcoin.domain.blockchain.model.Transaction
import com.github.dzivko1.dullcoin.domain.blockchain.usecase.SendCoinsResult
import com.github.dzivko1.dullcoin.util.withReentrantLock
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.security.PrivateKey

class DefaultBlockchainService(
    private val ownAddress: Address,
    private val privateKey: PrivateKey,
    private val networkService: NetworkService
) : BlockchainService {

    private val blocks = mutableMapOf<String, Block>()
    private val transactions = mutableMapOf<String, Transaction>()

    /**
     * Unspent transactions in which this address is the recipient.
     */
    private val relevantTransactions = mutableMapOf<String, Transaction>()

    private var currentBlock: Block = Block("")

    private val mutex = Mutex()

    override fun connectToNetwork() {
        networkService.connect()
    }

    override fun disconnectFromNetwork() {
        networkService.disconnect()
    }

    override suspend fun maintainBlockchain(): Unit = coroutineScope {
        downloadBlockchain()
        launch { listenForRequests() }
        launch { listenForTransactions() }
        launch { listenForBlocks() }
        launch { mine() }
    }

    private suspend fun downloadBlockchain() {
        networkService.sendRequest(
            request = GetBlockchainRequest,
            responseCount = 10,
            responseTimeout = 2000
        ) { response: GetBlockchainResponse ->
            blocks += response.blockchain
        }
    }

    private suspend fun listenForRequests() {
        networkService.getRequestFlow<GetBlockchainRequest>().collect { request ->
            mutex.withReentrantLock {
                networkService.sendResponse(request, GetBlockchainResponse(blocks))
            }
        }
    }

    private suspend fun listenForTransactions() {
        networkService.getMessageFlow<Transaction>().collect { transaction ->
            mutex.withReentrantLock {
                if (validateTransaction(transaction)) {
                    transactions[transaction.id] = transaction
                    if (transaction.outputs.any { it.recipient == ownAddress }) {
                        relevantTransactions[transaction.id] = transaction
                    }
                    currentBlock.addTransaction(transaction)
                }
            }
        }
    }

    private suspend fun listenForBlocks() {
        networkService.getMessageFlow<Block>().collect {
            mutex.withReentrantLock {

            }
        }
    }

    private suspend fun mine() {

    }

    private suspend fun validateTransaction(transaction: Transaction): Boolean = mutex.withReentrantLock {
        val inputSum = transaction.inputs.sumOf { input ->
            val inputTransaction = transactions[input.transactionId] ?: return false
            // We check that each input of this transaction isn't spent. It's spent if there is any existing transaction
            // which has it as an input.
            val spent = transactions.values.any { existingTransaction ->
                existingTransaction.inputs.contains(input)
            }
            if (spent) return false

            inputTransaction.outputs.getOrNull(input.outputIndex)
                ?.takeIf { it.recipient == transaction.sender }
                ?.amount ?: return false
        }
        val outputSum = transaction.outputs.sumOf { it.amount }
        if (inputSum < outputSum) return@withReentrantLock false

        return@withReentrantLock transaction.senderSignature?.let {
            Crypto.verify(transaction.hash(), transaction.sender.publicKey, it)
        } ?: false
    }

    private suspend fun validateBlock(block: Block): Boolean = mutex.withReentrantLock {
        TODO()
    }

    override suspend fun makeTransaction(
        amount: Int,
        recipient: Address,
        transactionFee: Int
    ): SendCoinsResult = mutex.withReentrantLock {
        val totalToSpend = amount + transactionFee

        val inputTransactions = mutableSetOf<Transaction>()
        var inputAmount = 0
        for (transaction in transactions.values) {
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

        networkService.broadcastMessage(transaction)
        transactions[transaction.id] = transaction
        currentBlock.addTransaction(transaction)
        relevantTransactions.values.removeAll(inputTransactions)

        return@withReentrantLock SendCoinsResult.Success
    }
}