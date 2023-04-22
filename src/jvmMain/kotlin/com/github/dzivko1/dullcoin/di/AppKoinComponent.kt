package com.github.dzivko1.dullcoin.di

import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent

abstract class AppKoinComponent(
    private val koinApp: KoinApplication
) : KoinComponent {
    override fun getKoin(): Koin = koinApp.koin
}