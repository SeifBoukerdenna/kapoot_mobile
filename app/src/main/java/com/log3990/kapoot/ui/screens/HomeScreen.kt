package com.log3990.kapoot.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun HomeScreen(
    username: String,
    onLogout: () -> Unit
) {
    Column {
        Text(text = "Hello, $username!")
        Button(onClick = { onLogout() }) {
            Text(text = "Logout")
        }
    }
}
