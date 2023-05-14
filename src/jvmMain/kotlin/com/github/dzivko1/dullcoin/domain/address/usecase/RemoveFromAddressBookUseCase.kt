package com.github.dzivko1.dullcoin.domain.address.usecase

import com.github.dzivko1.dullcoin.domain.address.AddressRepository

class RemoveFromAddressBookUseCase(
    private val addressRepository: AddressRepository
) {
    suspend operator fun invoke(name: String) {
        addressRepository.removeAddress(name)
    }
}