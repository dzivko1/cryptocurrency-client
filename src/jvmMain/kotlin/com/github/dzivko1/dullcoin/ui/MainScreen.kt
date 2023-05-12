package com.github.dzivko1.dullcoin.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen() {
    Scaffold {
        Row(
            Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                Modifier
                    .fillMaxHeight()
                    .weight(0.5f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MoneyPanel(Modifier.fillMaxWidth())
                AddressBookPanel(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }

            HistoryPanel(
                Modifier
                    .weight(0.5f)
                    .fillMaxSize()
            )
        }
    }
}

