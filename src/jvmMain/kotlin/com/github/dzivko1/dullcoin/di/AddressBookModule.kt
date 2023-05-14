package com.github.dzivko1.dullcoin.di

import com.github.dzivko1.dullcoin.data.address.LocalAddressRepository
import com.github.dzivko1.dullcoin.domain.address.AddressRepository
import com.github.dzivko1.dullcoin.domain.address.usecase.AddToAddressBookUseCase
import com.github.dzivko1.dullcoin.domain.address.usecase.GetAddressBookUseCase
import com.github.dzivko1.dullcoin.domain.address.usecase.RemoveFromAddressBookUseCase
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun addressBookModule() = module {

    singleOf(::LocalAddressRepository) { bind<AddressRepository>() }

    factoryOf(::GetAddressBookUseCase)
    factoryOf(::AddToAddressBookUseCase)
    factoryOf(::RemoveFromAddressBookUseCase)
}