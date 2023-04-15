package com.github.dzivko1.dullcoin.di

import com.github.dzivko1.dullcoin.data.blockchain.DefaultBlockchainService
import com.github.dzivko1.dullcoin.domain.blockchain.BlockchainService
import com.github.dzivko1.dullcoin.domain.blockchain.usecase.GetBalanceUseCase
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val CoinModule = module {

    singleOf(::DefaultBlockchainService) { bind<BlockchainService>() }

    singleOf(::GetBalanceUseCase)
}