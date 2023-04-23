package com.github.dzivko1.dullcoin.domain.blockchain.usecase

import com.github.dzivko1.dullcoin.domain.blockchain.BlockchainService
import com.github.dzivko1.dullcoin.domain.blockchain.model.Address

class SendCoinsUseCase(
    private val blockchainService: BlockchainService
) {
    suspend operator fun invoke(amount: Int, recipient: Address, transactionFee: Int): SendCoinsResult {
        return blockchainService.makeTransaction(amount, recipient, transactionFee)
    }
}

enum class SendCoinsResult {
    Success, InsufficientFunds
}