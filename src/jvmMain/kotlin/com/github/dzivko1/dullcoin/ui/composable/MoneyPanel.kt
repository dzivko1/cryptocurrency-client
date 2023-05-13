package com.github.dzivko1.dullcoin.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.dzivko1.dullcoin.ui.MoneyUiState

@Composable
fun MoneyPanel(
    uiState: MoneyUiState,
    onSendAddressChange: (String) -> Unit,
    onAmountToSendChange: (String) -> Unit,
    onTransactionFeeChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Panel(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(uiState.ownAddress)
            Text(uiState.balance)
            Divider()
            Text("Send coins")
            TextField(
                value = uiState.sendAddress,
                onValueChange = onSendAddressChange,
                label = { Text("Address") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = uiState.amountToSend,
                    onValueChange = onAmountToSendChange,
                    label = { Text("Amount") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    value = uiState.transactionFee,
                    onValueChange = onTransactionFeeChange,
                    label = { Text("Fee") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = onSendClick) {
                    Text("Send")
                }
            }
        }
    }
}