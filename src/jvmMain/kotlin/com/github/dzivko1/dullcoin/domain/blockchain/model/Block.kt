package com.github.dzivko1.dullcoin.domain.blockchain.model

import com.github.dzivko1.dullcoin.crypto.Crypto
import kotlinx.serialization.Serializable

@Serializable
class Block(
    val prevHash: String
) {
    constructor(
        prevHash: String,
        initialTransactions: List<Transaction>
    ) : this(prevHash) {
        addTransactions(initialTransactions)
    }

    var timestamp: Long = 0L
    var nonce: Long = 0L

    private val _transactions = mutableListOf<Transaction>()
    val transactions = _transactions as List<Transaction>

    fun addTransaction(transaction: Transaction) {
        _transactions += transaction
    }

    fun addTransactions(transactions: List<Transaction>) {
        _transactions += transactions
    }

    fun clearTransactions() {
        _transactions.clear()
    }

    fun hash(): String {
        return Crypto.hash(prevHash + timestamp + nonce + transactions.joinToString { it.hash() })
    }
}
