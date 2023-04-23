package com.github.dzivko1.dullcoin.domain.blockchain

import com.github.dzivko1.dullcoin.domain.blockchain.model.Address
import com.github.dzivko1.dullcoin.domain.blockchain.usecase.SendCoinsResult

interface BlockchainService {

    fun connectToNetwork()

    fun disconnectFromNetwork()

    suspend fun maintainBlockchain()

    suspend fun makeTransaction(amount: Int, recipient: Address, transactionFee: Int): SendCoinsResult

}