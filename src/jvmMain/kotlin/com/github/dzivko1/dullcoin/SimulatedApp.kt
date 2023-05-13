package com.github.dzivko1.dullcoin

import androidx.compose.runtime.Composable
import com.github.dzivko1.dullcoin.di.AppKoinComponent
import com.github.dzivko1.dullcoin.theme.AppTheme
import com.github.dzivko1.dullcoin.ui.MainViewModel
import com.github.dzivko1.dullcoin.ui.composable.MainScreen
import org.koin.core.KoinApplication

class SimulatedApp(
    private val koinApp: KoinApplication
) : AppKoinComponent(koinApp) {

    @Composable
    fun Window() {
        androidx.compose.ui.window.Window(onCloseRequest = ::close) {
            val viewModel = koinApp.koin.get<MainViewModel>()

            AppTheme {
                MainScreen(viewModel)
            }
        }
    }

    private fun close() {

    }
}