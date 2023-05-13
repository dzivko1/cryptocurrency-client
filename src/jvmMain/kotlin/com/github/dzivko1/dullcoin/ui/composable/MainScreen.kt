package com.github.dzivko1.dullcoin.ui.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.dzivko1.dullcoin.ui.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel) {
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
                MoneyPanel(
                    uiState = viewModel.moneyUiState,
                    onSendAddressChange = viewModel::onSendAddressChange,
                    onAmountToSendChange = viewModel::onAmountToSendChange,
                    onTransactionFeeChange = viewModel::onTransactionFeeChange,
                    onSendClick = viewModel::sendCoins,
                    modifier = Modifier.fillMaxWidth()
                )
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

