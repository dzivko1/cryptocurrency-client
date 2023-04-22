package com.github.dzivko1.dullcoin.di

import com.github.dzivko1.dullcoin.data.network.NetworkService
import com.github.dzivko1.dullcoin.data.network.SimulatedNetworkService
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun networkModule() = module {

    singleOf(::SimulatedNetworkService) { bind<NetworkService>() }
}