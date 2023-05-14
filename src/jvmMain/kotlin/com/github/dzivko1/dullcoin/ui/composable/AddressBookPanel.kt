package com.github.dzivko1.dullcoin.ui.composable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.onClick
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.dzivko1.dullcoin.ui.AddressBookUiState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddressBookPanel(
    uiState: AddressBookUiState,
    onNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: (name: String) -> Unit,
    onAddressDoubleClick: (name: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Panel(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Address book")
            TextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = uiState.address,
                onValueChange = onAddressChange,
                label = { Text("Address") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = onSaveClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Save")
            }
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(uiState.entries) { entry ->
                    Row(Modifier.onClick(
                        onClick = {},
                        onDoubleClick = {onAddressDoubleClick(entry.address)}
                    )) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Name: ${entry.name}")
                            Text("Address: ${entry.address}")
                        }
                        IconButton(onClick = { onDeleteClick(entry.name) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }
}
