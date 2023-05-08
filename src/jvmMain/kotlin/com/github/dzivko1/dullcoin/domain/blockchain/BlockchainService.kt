package com.github.dzivko1.dullcoin.domain.blockchain

import com.github.dzivko1.dullcoin.domain.blockchain.model.Address
import com.github.dzivko1.dullcoin.domain.blockchain.usecase.SendCoinsResult
import kotlinx.coroutines.flow.Flow

interface BlockchainService {

    val balanceFlow: Flow<Int>

    fun connectToNetwork()

    fun disconnectFromNetwork()

    fun startBlockchainMaintenance()

    suspend fun makeTransaction(amount: Int, recipient: Address, transactionFee: Int): SendCoinsResult

}