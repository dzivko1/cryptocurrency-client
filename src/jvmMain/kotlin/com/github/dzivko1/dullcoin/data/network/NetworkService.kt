package com.github.dzivko1.dullcoin.data.network

import kotlinx.coroutines.flow.Flow

interface NetworkService {

    fun connect()

    fun disconnect()

    fun getMessageFlow(): Flow<String>

    suspend fun sendMessage(message: String)

}