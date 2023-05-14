package com.github.dzivko1.dullcoin.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.dzivko1.dullcoin.domain.address.model.AddressBookEntry
import com.github.dzivko1.dullcoin.domain.address.usecase.AddToAddressBookUseCase
import com.github.dzivko1.dullcoin.domain.address.usecase.GetAddressBookUseCase
import com.github.dzivko1.dullcoin.domain.address.usecase.RemoveFromAddressBookUseCase
import com.github.dzivko1.dullcoin.domain.blockchain.model.Address
import com.github.dzivko1.dullcoin.domain.blockchain.usecase.GetBalanceUseCase
import com.github.dzivko1.dullcoin.domain.blockchain.usecase.SendCoinsResult
import com.github.dzivko1.dullcoin.domain.blockchain.usecase.SendCoinsUseCase
import com.github.dzivko1.dullcoin.domain.core.usecase.InitializeAppUseCase
import com.github.dzivko1.dullcoin.ui.snackbar.SnackbarState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(
    private val address: Address,
    private val initializeAppUseCase: InitializeAppUseCase,
    private val getBalanceUseCase: GetBalanceUseCase,
    private val sendCoinsUseCase: SendCoinsUseCase,
    private val getAddressBookUseCase: GetAddressBookUseCase,
    private val addToAddressBookUseCase: AddToAddressBookUseCase,
    private val removeFromAddressBookUseCase: RemoveFromAddressBookUseCase
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

    var addressBookUiState by mutableStateOf(AddressBookUiState())
        private set

    init {
        coroutineScope.launch {
            initializeAppUseCase.invoke()
            getBalanceUseCase().collect { balance ->
                moneyUiState = moneyUiState.copy(balance = "Balance: ${balance.toMoneyString()}")
            }
        }
        coroutineScope.launch {
            getAddressBookUseCase().collect {addresses ->
                addressBookUiState = addressBookUiState.copy(entries = addresses)
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
                amount = moneyUiState.amountToSend.toLong(),
                recipient = Address(moneyUiState.sendAddress),
                transactionFee = moneyUiState.transactionFee.toLong()
            )) {
                SendCoinsResult.Success -> showSnackbar("Coins sent!")
                SendCoinsResult.InsufficientFunds -> showSnackbar("You don't have enough coins!")
            }
        }
    }

    fun onNameChange(name: String) {
        addressBookUiState = addressBookUiState.copy(name = name)
    }

    fun onAddressChange(address: String) {
        addressBookUiState = addressBookUiState.copy(address = address)
    }

    fun saveAddress() {
        coroutineScope.launch {
            addToAddressBookUseCase.invoke(AddressBookEntry(addressBookUiState.name, addressBookUiState.address))
        }
    }

    fun deleteAddress(name: String) {
        coroutineScope.launch {
            removeFromAddressBookUseCase.invoke(name)
        }
    }

    fun fillAddress(address: String) {
        addressBookUiState = addressBookUiState.copy(address = address)
        moneyUiState = moneyUiState.copy(sendAddress = address)
    }

    private fun showSnackbar(message: String) {
        snackbar = SnackbarState(message)
    }

    fun consumeSnackbar() {
        snackbar = null
    }
}