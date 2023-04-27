package com.github.dzivko1.dullcoin.data.blockchain.model

import com.github.dzivko1.dullcoin.domain.blockchain.model.Transaction
import kotlinx.serialization.Serializable

@Serializable
object GetUnconfirmedTransactions

@Serializable
data class GetUnconfirmedTransactionsResponse(
    val unconfirmedTransactions: List<Transaction>
)