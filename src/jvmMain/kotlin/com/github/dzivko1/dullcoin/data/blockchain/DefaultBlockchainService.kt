package com.github.dzivko1.dullcoin.data.blockchain

import com.github.dzivko1.dullcoin.data.network.NetworkService
import com.github.dzivko1.dullcoin.domain.blockchain.BlockchainService
import com.github.dzivko1.dullcoin.domain.blockchain.model.Block
import com.github.dzivko1.dullcoin.domain.blockchain.model.Transaction
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class DefaultBlockchainService(
    private val networkService: NetworkService
) : BlockchainService {

    private var block: Block = Block("")

    override suspend fun maintainBlockchain(): Unit = coroutineScope {
        launch { listenForTransactions() }
        launch { listenForBlocks() }
        launch { mine() }
    }

    private suspend fun listenForTransactions() {
        networkService.getMessageFlow<Transaction>().collect { transaction ->
            if (validateTransaction(transaction)) {
                block.addTransaction(transaction)
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
        TODO()
    }

    private fun validateBlock(block: Block): Boolean {
        TODO()
    }
}