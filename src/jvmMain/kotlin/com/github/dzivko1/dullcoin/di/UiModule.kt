package com.github.dzivko1.dullcoin.di

import com.github.dzivko1.dullcoin.ui.ClientViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun uiModule() = module {
    singleOf(::ClientViewModel)
}