package com.github.dzivko1.dullcoin.data.core.network

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

interface NetworkService {

    suspend fun connect()

    suspend fun disconnect()

    fun <T> getMessageFlow(messageSerializer: KSerializer<T>): Flow<T>

    fun <T> getRequestFlow(requestSerializer: KSerializer<T>): Flow<Request<T>>

    /**
     * Broadcasts a serializable message to all peers in the network without waiting for responses.
     *
     * @param message message to be sent
     * @param messageSerializer the serializer for the message
     */
    suspend fun <T> broadcastMessage(message: T, messageSerializer: KSerializer<T>)

    /**
     * Sends a serializable request message and listens for responses until an exit condition is met.
     *
     * @param request request message to be sent
     * @param requestSerializer the serializer for the request
     * @param responseSerializer the serializer for the responses
     * @param responseCount maximum number of responses to listen for
     * @param responseTimeout maximum amount of time to listen for responses
     * @param onResponse function to be called for every accepted response
     */
    suspend fun <T, R> sendRequest(
        request: T,
        requestSerializer: KSerializer<T>,
        responseSerializer: KSerializer<R>,
        responseCount: Int,
        responseTimeout: Int,
        onResponse: suspend (R) -> Unit
    )

    /**
     * Sends a response message for the given [Request].
     */
    suspend fun <T, Q> sendResponse(request: Request<Q>, response: T, responseSerializer: KSerializer<T>)

}

inline fun <reified T> NetworkService.getMessageFlow(): Flow<T> {
    return getMessageFlow(Json.serializersModule.serializer())
}

inline fun <reified T> NetworkService.getRequestFlow(): Flow<Request<T>> {
    return getRequestFlow(Json.serializersModule.serializer())
}

/**
 * Broadcasts a serializable message to all peers in the network without waiting for responses.
 * The serializer for the message is inferred through reified type parameter.
 */
suspend inline fun <reified T> NetworkService.broadcastMessage(message: T) {
    broadcastMessage(message, Json.serializersModule.serializer())
}

/**
 * Sends a serializable request message and listens for responses until an exit condition is met.
 * The serializers for the model and response are inferred through reified type parameters.
 *
 * @param request request message to be sent
 * @param responseCount maximum number of responses to listen for
 * @param responseTimeout maximum amount of time to listen for responses
 * @param onResponse function to be called for every accepted response
 */
suspend inline fun <reified T, reified R> NetworkService.sendRequest(
    request: T,
    responseCount: Int,
    responseTimeout: Int,
    noinline onResponse: suspend (R) -> Unit
) {
    sendRequest(
        request = request,
        requestSerializer = Json.serializersModule.serializer<T>(),
        responseSerializer = Json.serializersModule.serializer<R>(),
        responseCount = responseCount,
        responseTimeout = responseTimeout,
        onResponse = onResponse
    )
}

/**
 * Sends a response message for the given [Request].
 * The serializer for the response message is inferred through reified type parameter.
 */
suspend inline fun <reified T, Q> NetworkService.sendResponse(request: Request<Q>, response: T) {
    sendResponse(request, response, Json.serializersModule.serializer())
}