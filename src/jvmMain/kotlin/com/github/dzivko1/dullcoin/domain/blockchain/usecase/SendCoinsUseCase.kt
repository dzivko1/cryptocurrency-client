package com.github.dzivko1.dullcoin.domain.blockchain.usecase

import com.github.dzivko1.dullcoin.domain.blockchain.BlockchainService
import com.github.dzivko1.dullcoin.domain.blockchain.model.Address

class SendCoinsUseCase(
    private val blockchainService: BlockchainService
) {
    suspend operator fun invoke(amount: Long, recipient: Address, transactionFee: Long): SendCoinsResult {
        return blockchainService.makeTransaction(amount, recipient, transactionFee)
    }
}

enum class SendCoinsResult {
    Success, InsufficientFunds
}