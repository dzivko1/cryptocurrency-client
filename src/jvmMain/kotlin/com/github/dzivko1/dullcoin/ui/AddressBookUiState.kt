package com.github.dzivko1.dullcoin.ui

import com.github.dzivko1.dullcoin.domain.address.model.AddressBookEntry

data class AddressBookUiState(
    val name: String = "",
    val address: String = "",
    val entries: List<AddressBookEntry> = emptyList()
)