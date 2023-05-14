package com.github.dzivko1.dullcoin.domain.address.usecase

import com.github.dzivko1.dullcoin.domain.address.AddressRepository
import com.github.dzivko1.dullcoin.domain.address.model.AddressBookEntry
import kotlinx.coroutines.flow.Flow

class GetAddressBookUseCase(
    private val addressRepository: AddressRepository
) {
    operator fun invoke(): Flow<List<AddressBookEntry>> {
        return addressRepository.getAddressFlow()
    }
}