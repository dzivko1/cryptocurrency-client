package com.github.dzivko1.dullcoin

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.dzivko1.dullcoin.di.AddressModule
import com.github.dzivko1.dullcoin.di.CoinModule
import com.github.dzivko1.dullcoin.di.NetworkModule
import org.koin.core.context.startKoin

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello, World!") }

    MaterialTheme {
        Button(onClick = {
            text = "Hello, Desktop!"
        }) {
            Text(text)
        }
    }
}

fun main() = application {
    startKoin {
        modules(NetworkModule, CoinModule, AddressModule)
    }

    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
