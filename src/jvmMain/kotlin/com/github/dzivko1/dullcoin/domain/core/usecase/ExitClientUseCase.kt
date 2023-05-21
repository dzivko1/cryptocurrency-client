package com.github.dzivko1.dullcoin.domain.core.usecase

import com.github.dzivko1.dullcoin.domain.blockchain.BlockchainService

class ExitClientUseCase(
    private val blockchainService: BlockchainService
) {
    suspend operator fun invoke() {
        blockchainService.disconnectFromNetwork()
        blockchainService.stopBlockchainMaintenance()
    }
}