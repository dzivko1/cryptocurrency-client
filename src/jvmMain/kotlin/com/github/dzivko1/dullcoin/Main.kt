package com.github.dzivko1.dullcoin

import androidx.compose.ui.window.application
import com.github.dzivko1.dullcoin.di.*
import org.koin.core.context.startKoin
import org.koin.dsl.koinApplication

fun main() = application {
    startKoin {}

    val testApp = SimulatedApp(
        koinApplication {
            modules(GlobalModule, uiModule(), networkModule(), blockchainModule(), addressBookModule())
        }
    )
    testApp.Window()
}
