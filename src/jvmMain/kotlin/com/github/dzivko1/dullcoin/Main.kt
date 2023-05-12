package com.github.dzivko1.dullcoin

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.dzivko1.dullcoin.di.GlobalModule
import com.github.dzivko1.dullcoin.di.addressBookModule
import com.github.dzivko1.dullcoin.di.blockchainModule
import com.github.dzivko1.dullcoin.di.networkModule
import com.github.dzivko1.dullcoin.theme.AppTheme
import com.github.dzivko1.dullcoin.ui.MainScreen
import org.koin.core.context.startKoin
import org.koin.dsl.koinApplication

@Composable
@Preview
fun App() {
    AppTheme {
        MainScreen()
    }
}

fun main() = application {
    startKoin {}

    SimulatedApp(
        koinApplication {
            modules(GlobalModule, networkModule(), blockchainModule(), addressBookModule())
        }
    )

    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
