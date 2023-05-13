package com.github.dzivko1.dullcoin.ui

data class MoneyUiState(
    val ownAddress: String = "",
    val balance: String = "",
    val sendAddress: String = "",
    val amountToSend: String = "",
    val transactionFee: String = ""
)
