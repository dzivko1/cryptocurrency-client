package com.github.dzivko1.dullcoin.data.network

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

interface NetworkService {

    fun connect()

    fun disconnect()

    fun <T> getMessageFlow(messageSerializer: KSerializer<T>): Flow<T>

    suspend fun <T> sendMessage(message: T, messageSerializer: KSerializer<T>)

}

inline fun <reified T> NetworkService.getMessageFlow(): Flow<T> {
    return getMessageFlow(Json.serializersModule.serializer())
}

suspend inline fun <reified T> NetworkService.sendMessage(message: T) {
    sendMessage(message, Json.serializersModule.serializer())
}