package com.github.dzivko1.dullcoin.di

import com.github.dzivko1.dullcoin.data.network.NetworkService
import com.github.dzivko1.dullcoin.data.network.SimulatedInternet
import com.github.dzivko1.dullcoin.data.network.SimulatedNetworkService
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val NetworkModule = module {

    singleOf(::SimulatedInternet)

    factoryOf(::SimulatedNetworkService) { bind<NetworkService>() }
}