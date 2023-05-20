package com.github.dzivko1.dullcoin.domain.blockchain.usecase

import com.github.dzivko1.dullcoin.domain.blockchain.BlockchainService
import com.github.dzivko1.dullcoin.domain.blockchain.model.Transaction

class GetUserTransactionsUseCase(
    private val blockchainService: BlockchainService
) {
    operator fun invoke(): List<Transaction> = blockchainService.getUserTransactions()
}