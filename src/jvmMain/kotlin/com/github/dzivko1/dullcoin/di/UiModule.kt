package com.github.dzivko1.dullcoin.di

import com.github.dzivko1.dullcoin.ui.MainViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

fun uiModule() = module {
    factoryOf(::MainViewModel)
}