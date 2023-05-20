package com.github.dzivko1.dullcoin.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.dzivko1.dullcoin.ui.HistoryUiState

@Composable
fun HistoryPanel(
    uiState: HistoryUiState,
    modifier: Modifier = Modifier
) {
    Panel(modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("History")
            Divider()
            LazyColumn(
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.transactions) {transaction ->
                    Row {
                        Text(
                            text = "${transaction.sender} → ${transaction.recipient}",
                            Modifier.weight(1f)
                        )
                        Text(transaction.amount)
                    }
                }
            }
        }
    }
}