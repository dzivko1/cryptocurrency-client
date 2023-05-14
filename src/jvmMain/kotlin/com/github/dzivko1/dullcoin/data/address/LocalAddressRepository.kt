package com.github.dzivko1.dullcoin.data.address

import com.github.dzivko1.dullcoin.domain.address.AddressRepository
import com.github.dzivko1.dullcoin.domain.address.model.AddressBookEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalAddressRepository : AddressRepository {

    private val _addressFlow = MutableStateFlow(listOf<AddressBookEntry>())
    private val addressFlow = _addressFlow.asStateFlow()

    override fun getAddressFlow(): Flow<List<AddressBookEntry>> {
        return addressFlow
    }

    override suspend fun saveAddress(address: AddressBookEntry) {
        val existingAddress = _addressFlow.value.find { it.name == address.name }
        val newAddresses =
            if (existingAddress == null) _addressFlow.value + address
            else _addressFlow.value - existingAddress + address
        _addressFlow.value = newAddresses
    }

    override suspend fun removeAddress(name: String) {
        _addressFlow.value = _addressFlow.value.filter { it.name != name }
    }
}