package com.github.dzivko1.dullcoin.domain.blockchain.model

import kotlinx.serialization.Serializable

@Serializable
class Block(
    val prevHash: String
) {
    var timestamp: Long = 0L
        private set

    var nonce: Long = 0L

    private val _transactions = mutableListOf<Transaction>()
    val transaction = _transactions as List<Transaction>

    fun addTransaction(transaction: Transaction) {
        _transactions += transaction
    }
}
