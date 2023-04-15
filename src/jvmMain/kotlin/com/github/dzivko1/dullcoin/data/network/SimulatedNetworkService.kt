package com.github.dzivko1.dullcoin.data.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.util.*

class SimulatedNetworkService(
    private val internet: SimulatedInternet
) : NetworkService {

    private val id = UUID.randomUUID().toString()
    private val networkId = "main_network"

    private var messageFlow: Flow<String> = emptyFlow()

    override fun connect() {
        messageFlow = internet.connect(id, networkId)
    }

    override fun disconnect() {
        internet.disconnect(id, networkId)
        messageFlow = emptyFlow()
    }

    override fun <T : Any> getMessageFlow(messageSerializer: KSerializer<T>): Flow<T> {
        return messageFlow.mapNotNull {
            Json.runCatching {
                decodeFromString(messageSerializer, it)
            }.getOrNull()
        }
    }

    override suspend fun sendMessage(message: String) {
        internet.broadcastMessage(networkId, message)
    }

}