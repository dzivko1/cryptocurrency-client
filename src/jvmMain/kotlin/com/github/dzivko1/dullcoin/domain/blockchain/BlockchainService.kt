package com.github.dzivko1.dullcoin.domain.blockchain

interface BlockchainService {

    suspend fun maintainBlockchain()

}