package com.github.dzivko1.dullcoin.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.dzivko1.dullcoin.domain.blockchain.model.Address
import com.github.dzivko1.dullcoin.domain.blockchain.usecase.GetBalanceUseCase
import com.github.dzivko1.dullcoin.domain.blockchain.usecase.SendCoinsResult
import com.github.dzivko1.dullcoin.domain.blockchain.usecase.SendCoinsUseCase
import com.github.dzivko1.dullcoin.ui.snackbar.SnackbarState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(
    private val address: Address,
    private val getBalanceUseCase: GetBalanceUseCase,
    private val sendCoinsUseCase: SendCoinsUseCase
) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    var snackbar by mutableStateOf<SnackbarState?>(null)
        private set

    var moneyUiState by mutableStateOf(
        MoneyUiState(
            ownAddress = address.stringRepresentation
        )
    )
        private set

    init {
        coroutineScope.launch {
            getBalanceUseCase().collect { balance ->
                moneyUiState = moneyUiState.copy(balance = "Balance: ${balance.toMoneyString()}")
            }
        }
    }

    fun onSendAddressChange(address: String) {
        moneyUiState = moneyUiState.copy(sendAddress = address)
    }

    fun onAmountToSendChange(amount: String) {
        moneyUiState = moneyUiState.copy(amountToSend = amount)
    }

    fun onTransactionFeeChange(fee: String) {
        moneyUiState = moneyUiState.copy(transactionFee = fee)
    }

    fun sendCoins() {
        coroutineScope.launch {
            when (sendCoinsUseCase.invoke(
                amount = moneyUiState.amountToSend.toInt(),
                recipient = Address(moneyUiState.sendAddress),
                transactionFee = moneyUiState.transactionFee.toInt()
            )) {
                SendCoinsResult.Success -> showSnackbar("Coins sent!")
                SendCoinsResult.InsufficientFunds -> showSnackbar("You don't have enough coins!")
            }
        }
    }

    private fun showSnackbar(message: String) {
        snackbar = SnackbarState(message)
    }

    fun consumeSnackbar() {
        snackbar = null
    }
}