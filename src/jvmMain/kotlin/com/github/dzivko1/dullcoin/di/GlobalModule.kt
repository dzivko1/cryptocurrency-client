package com.github.dzivko1.dullcoin.di

import com.github.dzivko1.dullcoin.data.core.network.SimulatedInternet
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val GlobalModule = module {

    singleOf(::SimulatedInternet)
}