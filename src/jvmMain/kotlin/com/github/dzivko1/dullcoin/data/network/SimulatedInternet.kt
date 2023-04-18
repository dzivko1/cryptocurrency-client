package com.github.dzivko1.dullcoin.data.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class SimulatedInternet {

    private val networks = mutableMapOf<String, Network>()

    fun connect(clientId: String, networkId: String): Flow<String> {
        val network = networks.computeIfAbsent(networkId) { Network() }
        return network.connectClient(clientId)
    }

    fun disconnect(clientId: String, networkId: String) {
        networks[networkId]?.disconnectClient(clientId)
    }

    suspend fun broadcastMessage(networkId: String, message: String) {
        networks[networkId]?.broadcastMessage(message)
    }

    private class Network {
        val clients = mutableMapOf<String, MutableSharedFlow<String>>()

        fun connectClient(clientId: String): Flow<String> {
            return clients.computeIfAbsent(clientId) { MutableSharedFlow() }
        }

        fun disconnectClient(clientId: String) {
            clients.remove(clientId)
        }

        suspend fun broadcastMessage(message: String) {
            clients.forEach { (_, flow) ->
                flow.emit(message)
            }
        }
    }
}