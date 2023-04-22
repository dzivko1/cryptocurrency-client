package com.github.dzivko1.dullcoin.di

import com.github.dzivko1.dullcoin.data.blockchain.DefaultBlockchainService
import com.github.dzivko1.dullcoin.domain.blockchain.BlockchainService
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun blockchainModule() = module {

    singleOf(::DefaultBlockchainService) { bind<BlockchainService>() }
}