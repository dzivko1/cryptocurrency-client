package com.github.dzivko1.dullcoin.data.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class SimulatedInternet {

    private val networks = mutableMapOf<String, Network>()

    fun connect(clientId: String, networkId: String): Flow<String> {
        val network = networks.computeIfAbsent(networkId) { Network() }
        return MutableSharedFlow<String>().also {
            network.clients += (clientId to it)
        }
    }

    fun disconnect(clientId: String, networkId: String) {
        networks[networkId]?.clients?.remove(clientId)
    }

    suspend fun broadcastMessage(networkId: String, message: String) {
        networks[networkId]?.clients?.forEach { (_, flow) ->
            flow.emit(message)
        }
    }

    private class Network {
        val clients = mutableMapOf<String, MutableSharedFlow<String>>()
    }
}