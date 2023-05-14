package com.github.dzivko1.dullcoin.domain.address

import com.github.dzivko1.dullcoin.domain.address.model.AddressBookEntry
import kotlinx.coroutines.flow.Flow

interface AddressRepository {

    fun getAddressFlow(): Flow<List<AddressBookEntry>>

    suspend fun saveAddress(address: AddressBookEntry)

    suspend fun removeAddress(name: String)
}