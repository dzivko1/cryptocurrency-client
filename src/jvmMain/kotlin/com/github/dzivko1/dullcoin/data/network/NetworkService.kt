package com.github.dzivko1.dullcoin.data.network

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer

interface NetworkService {

    fun connect()

    fun disconnect()

    fun <T : Any> getMessageFlow(messageSerializer: KSerializer<T>): Flow<T>

    suspend fun sendMessage(message: String)

}