package com.github.dzivko1.dullcoin.domain.address.usecase

import com.github.dzivko1.dullcoin.domain.address.AddressRepository
import com.github.dzivko1.dullcoin.domain.address.model.AddressBookEntry

class AddToAddressBookUseCase(
    private val addressRepository: AddressRepository
) {
    suspend operator fun invoke(address: AddressBookEntry) {
        addressRepository.saveAddress(address)
    }
}