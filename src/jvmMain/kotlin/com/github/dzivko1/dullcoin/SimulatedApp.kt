package com.github.dzivko1.dullcoin

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import com.github.dzivko1.dullcoin.di.AppKoinComponent
import com.github.dzivko1.dullcoin.theme.AppTheme
import com.github.dzivko1.dullcoin.ui.ClientViewModel
import com.github.dzivko1.dullcoin.ui.composable.MainScreen
import org.koin.core.KoinApplication

class SimulatedApp(
    private val koinApp: KoinApplication,
    private val index: Int
) : AppKoinComponent(koinApp) {

    private val viewModel = koinApp.koin.get<ClientViewModel>()

    @Composable
    fun Window(onCloseRequest: () -> Unit) {
        androidx.compose.ui.window.Window(
            state = rememberWindowState(
                width = 1000.dp,
                height = 650.dp
            ),
            title = "DullCoin Client #${index + 1}",
            onCloseRequest = {
                onCloseRequest()
                viewModel.exit()
            }
        ) {
            AppTheme {
                MainScreen(viewModel)
            }
        }
    }
}