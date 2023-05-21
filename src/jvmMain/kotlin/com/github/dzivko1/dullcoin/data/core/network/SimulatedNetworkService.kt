package com.github.dzivko1.dullcoin.data.core.network

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.util.*

@OptIn(ExperimentalSerializationApi::class)
class SimulatedNetworkService(
    private val internet: SimulatedInternet
) : NetworkService {

    private val address = UUID.randomUUID().toString()
    private val networkId = "main_network"

    private var messageFlow: Flow<String> = emptyFlow()

    override suspend fun connect() {
        messageFlow = internet.connect(address, networkId)
    }

    override suspend fun disconnect() {
        internet.disconnect(address, networkId)
        messageFlow = emptyFlow()
    }

    override fun <T> getMessageFlow(messageSerializer: KSerializer<T>): Flow<T> {
        return messageFlow
            .map { Json.decodeFromString<Packet>(it) }
            .filter { it.requestCode == null && it.serialName == messageSerializer.descriptor.serialName }
            .mapNotNull { packet ->
                runCatching {
                    Json.decodeFromString(messageSerializer, packet.jsonBody)
                }.getOrNull()
            }
    }

    override fun <T> getRequestFlow(requestSerializer: KSerializer<T>): Flow<Request<T>> {
        return messageFlow
            .map { Json.decodeFromString<Packet>(it) }
            .filter { it.requestCode != null && it.serialName == requestSerializer.descriptor.serialName }
            .mapNotNull { packet ->
                runCatching {
                    Json.decodeFromString(requestSerializer, packet.jsonBody)
                }.getOrNull()?.let {
                    Request(
                        fromAddress = packet.fromAddress,
                        code = packet.requestCode!!,
                        data = it
                    )
                }
            }
    }

    override suspend fun <T> broadcastMessage(message: T, messageSerializer: KSerializer<T>) {
        val packet = Packet(
            fromAddress = address,
            requestCode = null,
            serialName = messageSerializer.descriptor.serialName,
            jsonBody = Json.encodeToString(messageSerializer, message)
        )
        internet.broadcastMessage(
            networkId = networkId,
            senderAddress = address,
            message = Json.encodeToString(packet)
        )
    }

    override suspend fun <T, R> sendRequest(
        request: T,
        requestSerializer: KSerializer<T>,
        responseSerializer: KSerializer<R>,
        responseCount: Int,
        responseTimeout: Int,
        onResponse: suspend (R) -> Unit
    ) {
        require(responseCount > 0) { "responseCount must be a positive integer" }
        require(responseTimeout > 0) { "responseTimeout must be a positive integer" }

        runCatching {
            coroutineScope {
                val code = UUID.randomUUID().toString()
                var responses = 0
                var timedOut = false
                var processingResponse = false

                launch {
                    delay(responseTimeout.toLong())
                    if (!processingResponse) this@coroutineScope.cancel()
                    else timedOut = true
                }

                launch {
                    messageFlow
                        .map { Json.decodeFromString<Packet>(it) }
                        .filter { it.requestCode == code && it.serialName == responseSerializer.descriptor.serialName }
                        .mapNotNull { responsePacket ->
                            Json.runCatching {
                                decodeFromString(responseSerializer, responsePacket.jsonBody)
                            }.getOrNull()
                        }
                        .collect {
                            processingResponse = true
                            onResponse(it)
                            processingResponse = false
                            if (timedOut || ++responses == responseCount) this@coroutineScope.cancel()
                        }
                }

                val requestPacket = Packet(
                    fromAddress = address,
                    requestCode = code,
                    serialName = requestSerializer.descriptor.serialName,
                    jsonBody = Json.encodeToString(requestSerializer, request)
                )
                internet.broadcastMessage(
                    networkId = networkId,
                    senderAddress = address,
                    message = Json.encodeToString(requestPacket)
                )
            }
        }
    }

    override suspend fun <T, Q> sendResponse(request: Request<Q>, response: T, responseSerializer: KSerializer<T>) {
        val packet = Packet(
            fromAddress = address,
            requestCode = request.code,
            serialName = responseSerializer.descriptor.serialName,
            jsonBody = Json.encodeToString(responseSerializer, response)
        )
        internet.sendMessage(
            networkId = networkId,
            address = request.fromAddress,
            message = Json.encodeToString(packet)
        )
    }

    @Serializable
    private data class Packet(
        val fromAddress: String,
        val requestCode: String?,
        val serialName: String,
        val jsonBody: String
    )
}