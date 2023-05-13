package com.github.dzivko1.dullcoin.ui.composable

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AddressBookPanel(
    modifier: Modifier = Modifier
) {
    Panel(modifier) {
        Text("address")
    }
}