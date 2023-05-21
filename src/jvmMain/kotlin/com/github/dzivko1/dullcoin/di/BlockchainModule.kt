package com.github.dzivko1.dullcoin.di

import com.github.dzivko1.dullcoin.crypto.Crypto
import com.github.dzivko1.dullcoin.data.blockchain.DefaultBlockchainService
import com.github.dzivko1.dullcoin.domain.blockchain.BlockchainService
import com.github.dzivko1.dullcoin.domain.blockchain.model.Address
import com.github.dzivko1.dullcoin.domain.blockchain.usecase.GetBalanceUseCase
import com.github.dzivko1.dullcoin.domain.blockchain.usecase.GetUserTransactionsUseCase
import com.github.dzivko1.dullcoin.domain.blockchain.usecase.SendCoinsUseCase
import com.github.dzivko1.dullcoin.domain.core.usecase.ExitClientUseCase
import com.github.dzivko1.dullcoin.domain.core.usecase.InitializeClientUseCase
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey

fun blockchainModule() = module {

    // Technically, this should be generated only on first start of a simulated client app and permanently stored for
    // each of them, but for the purposes of this project, this should suffice
    single<KeyPair> { Crypto.generateKeyPair() }

    single<PublicKey> { get<KeyPair>().public }
    single<PrivateKey> { get<KeyPair>().private }

    single<Address> { Address(get<PublicKey>()) }

    singleOf(::DefaultBlockchainService) { bind<BlockchainService>() }

    factoryOf(::InitializeClientUseCase)
    factoryOf(::ExitClientUseCase)
    factoryOf(::GetBalanceUseCase)
    factoryOf(::SendCoinsUseCase)
    factoryOf(::GetUserTransactionsUseCase)
}