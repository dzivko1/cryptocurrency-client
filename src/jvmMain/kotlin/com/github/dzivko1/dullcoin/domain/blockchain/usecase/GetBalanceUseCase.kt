package com.github.dzivko1.dullcoin.domain.blockchain.usecase

import com.github.dzivko1.dullcoin.domain.blockchain.BlockchainService
import kotlinx.coroutines.flow.Flow

class GetBalanceUseCase(
    private val blockchainService: BlockchainService
) {
    operator fun invoke(): Flow<Int> = blockchainService.balanceFlow
}