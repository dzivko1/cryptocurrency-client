package com.github.dzivko1.dullcoin.data.core.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SimulatedInternet {

    private val networks = mutableMapOf<String, Network>()

    suspend fun connect(ownAddress: String, networkId: String): Flow<String> {
        val network = networks.computeIfAbsent(networkId) { Network() }
        return network.connectClient(ownAddress)
    }

    suspend fun disconnect(ownAddress: String, networkId: String) {
        networks[networkId]?.disconnectClient(ownAddress)
    }

    suspend fun sendMessage(networkId: String, address: String, message: String) {
        networks[networkId]?.sendMessage(address, message)
    }

    suspend fun broadcastMessage(networkId: String, senderAddress: String, message: String) {
        networks[networkId]?.broadcastMessage(senderAddress, message)
    }

    private class Network {
        private val clients = mutableMapOf<String, MutableSharedFlow<String>>()

        private val mutex = Mutex()

        suspend fun connectClient(ownAddress: String): Flow<String> = mutex.withLock {
            return clients.computeIfAbsent(ownAddress) { MutableSharedFlow() }
        }

        suspend fun disconnectClient(ownAddress: String) = mutex.withLock {
            clients.remove(ownAddress)
        }

        suspend fun sendMessage(address: String, message: String) = mutex.withLock {
            clients[address]?.emit(message)
        }

        suspend fun broadcastMessage(senderAddress: String, message: String) = mutex.withLock {
            clients.forEach { (id, flow) ->
                if (id != senderAddress) {
                    flow.emit(message)
                }
            }
        }
    }
}