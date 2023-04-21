package com.github.dzivko1.dullcoin.data.blockchain.model

import com.github.dzivko1.dullcoin.domain.blockchain.model.Block
import kotlinx.serialization.Serializable

@Serializable
object GetBlockchainRequest

@Serializable
data class GetBlockchainResponse(
    val blockchain: Map<String, Block>
)