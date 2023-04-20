package com.github.dzivko1.dullcoin.domain.blockchain

interface BlockchainService {

    fun connectToNetwork()

    fun disconnectFromNetwork()

    suspend fun maintainBlockchain()

}