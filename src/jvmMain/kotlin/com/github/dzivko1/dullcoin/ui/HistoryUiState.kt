package com.github.dzivko1.dullcoin.ui

data class HistoryUiState(
    val transactions: List<TransactionUi> = emptyList()
)

data class TransactionUi(
    val sender: String,
    val recipient: String,
    val amount: String
)
