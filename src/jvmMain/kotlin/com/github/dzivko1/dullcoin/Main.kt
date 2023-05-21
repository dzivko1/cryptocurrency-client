package com.github.dzivko1.dullcoin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.github.dzivko1.dullcoin.di.*
import com.github.dzivko1.dullcoin.theme.AppTheme
import org.koin.core.context.startKoin
import org.koin.dsl.koinApplication

@Composable
private fun MainWindow(
    clients: List<SimulatedApp>,
    onStartClientClick: () -> Unit,
    onCloseRequest: () -> Unit
) {
    Window(
        state = rememberWindowState(
            width = 350.dp,
            height = 150.dp
        ),
        title = "DullCoin Client Manager",
        resizable = false,
        onCloseRequest = onCloseRequest
    ) {
        AppTheme {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Running clients: ${clients.size}")
                Button(onClick = onStartClientClick) {
                    Text("Start new client")
                }
            }
        }
    }
}

fun main() = application {
    LaunchedEffect(Unit) {
        startKoin {}
    }

    val clients = remember { mutableStateListOf<SimulatedApp>() }
    var clientCounter by remember { mutableStateOf(0) }

    MainWindow(
        clients = clients,
        onStartClientClick = {
            clients += SimulatedApp(
                koinApplication {
                    modules(GlobalModule, uiModule(), networkModule(), blockchainModule(), addressBookModule())
                },
                index = clientCounter++
            )
        },
        onCloseRequest = this::exitApplication
    )

    clients.forEachIndexed { index, client ->
        client.Window(
            onCloseRequest = { clients.removeAt(index) }
        )
    }
}
