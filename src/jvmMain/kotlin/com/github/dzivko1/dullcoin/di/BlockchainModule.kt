package com.github.dzivko1.dullcoin.di

import com.github.dzivko1.dullcoin.crypto.Crypto
import com.github.dzivko1.dullcoin.data.blockchain.DefaultBlockchainService
import com.github.dzivko1.dullcoin.domain.blockchain.BlockchainService
import com.github.dzivko1.dullcoin.domain.blockchain.model.Address
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.security.KeyPair
import java.security.PrivateKey

fun blockchainModule() = module {

    // Technically, this should be generated only on first start of a simulated client app and permanently stored for
    // each of them, but for the purposes of this project, this should suffice
    single<KeyPair> { Crypto.generateKeyPair() }

    single<Address> { Address(get<KeyPair>().public) }
    single<PrivateKey> { get<KeyPair>().private }

    singleOf(::DefaultBlockchainService) { bind<BlockchainService>() }
}