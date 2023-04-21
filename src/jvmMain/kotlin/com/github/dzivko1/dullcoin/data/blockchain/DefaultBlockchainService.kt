package com.github.dzivko1.dullcoin.data.blockchain

import com.github.dzivko1.dullcoin.crypto.Crypto
import com.github.dzivko1.dullcoin.data.blockchain.model.GetBlockchainRequest
import com.github.dzivko1.dullcoin.data.blockchain.model.GetBlockchainResponse
import com.github.dzivko1.dullcoin.data.network.*
import com.github.dzivko1.dullcoin.domain.blockchain.BlockchainService
import com.github.dzivko1.dullcoin.domain.blockchain.model.Block
import com.github.dzivko1.dullcoin.domain.blockchain.model.Transaction
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class DefaultBlockchainService(
    private val networkService: NetworkService
) : BlockchainService {

    private val blocks = mutableMapOf<String, Block>()
    private val transactions = mutableMapOf<String, Transaction>()

    private var currentBlock: Block = Block("")

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
            networkService.sendResponse(request, GetBlockchainResponse(blocks))
        }
    }

    private suspend fun listenForTransactions() {
        networkService.getMessageFlow<Transaction>().collect { transaction ->
            if (validateTransaction(transaction)) {
                transactions[transaction.id] = transaction
                currentBlock.addTransaction(transaction)
            }
        }
    }

    private suspend fun listenForBlocks() {
        networkService.getMessageFlow<Block>().collect {

        }
    }

    private suspend fun mine() {

    }

    private fun validateTransaction(transaction: Transaction): Boolean {
        val inputSum = transaction.inputs.sumOf { input ->
            transactions[input.transactionId]
                ?.outputs?.getOrNull(input.outputIndex)
                ?.takeIf { it.recipientKey == transaction.senderKey }
                ?.value ?: 0
        }
        val outputSum = transaction.outputs.sumOf { it.value }
        if (inputSum < outputSum) return false

        return transaction.senderSignature?.let {
            Crypto.verify(transaction.hash(), transaction.senderKey, it)
        } ?: false
    }

    private fun validateBlock(block: Block): Boolean {
        TODO()
    }
}