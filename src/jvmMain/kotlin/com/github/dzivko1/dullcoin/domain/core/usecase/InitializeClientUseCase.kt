package com.github.dzivko1.dullcoin.domain.core.usecase

import com.github.dzivko1.dullcoin.domain.blockchain.BlockchainService

class InitializeAppUseCase(
    private val blockchainService: BlockchainService
) {
    suspend operator fun invoke() {
        blockchainService.connectToNetwork()
        blockchainService.startBlockchainMaintenance()
    }
}