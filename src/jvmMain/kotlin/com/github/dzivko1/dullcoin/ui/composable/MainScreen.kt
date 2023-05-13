package com.github.dzivko1.dullcoin.ui.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.dzivko1.dullcoin.ui.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val scaffoldState = rememberScaffoldState()
    val snackbarHostState = scaffoldState.snackbarHostState

    viewModel.snackbar?.let { snackbar ->
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar(
                message = snackbar.message
            )
            viewModel.consumeSnackbar()
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = { state ->
            SnackbarHost(state) {
                Snackbar(it)
            }
        }
    ) {
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

