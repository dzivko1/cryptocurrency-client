package com.github.dzivko1.dullcoin.data.blockchain

import com.github.dzivko1.dullcoin.domain.blockchain.model.Transaction

interface TransactionProvider {
    val transactions: Map<String, Transaction>
}