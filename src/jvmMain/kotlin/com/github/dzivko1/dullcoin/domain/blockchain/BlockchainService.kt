package com.github.dzivko1.dullcoin.domain.blockchain

import com.github.dzivko1.dullcoin.domain.blockchain.model.Address
import com.github.dzivko1.dullcoin.domain.blockchain.model.Transaction
import com.github.dzivko1.dullcoin.domain.blockchain.usecase.SendCoinsResult
import kotlinx.coroutines.flow.Flow

interface BlockchainService {

    val balanceFlow: Flow<Long>

    suspend fun connectToNetwork()

    suspend fun disconnectFromNetwork()

    fun startBlockchainMaintenance()

    fun stopBlockchainMaintenance()

    fun getUserTransactions(): List<Transaction>

    suspend fun makeTransaction(amount: Long, recipient: Address, transactionFee: Long): SendCoinsResult

}