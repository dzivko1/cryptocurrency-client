package com.github.dzivko1.dullcoin.data.blockchain.model

import kotlinx.serialization.Serializable

@Serializable
object GetBlockchainRequest

@Serializable
data class GetBlockchainResponse(
    val test: String
)