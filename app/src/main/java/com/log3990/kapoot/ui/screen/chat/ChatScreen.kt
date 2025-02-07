// ChatScreen.kt
package com.log3990.kapoot.ui.screen.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.log3990.kapoot.ui.component.*
import com.log3990.kapoot.ui.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    username: String,
    onLogout: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    var message by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsState()

    LaunchedEffect(username) {
        viewModel.connect(username)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.disconnect()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBox,
                        contentDescription = null
                    )
                    Text(
                        text = "Logged in as: $username",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            actions = {
                IconButton(onClick = {
                    viewModel.disconnect()
                    onLogout()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Logout"
                    )
                }
            }
        )

        // Messages List
        MessageList(
            messages = messages,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        // Input Area
        MessageInputBar(
            message = message,
            onMessageChange = { message = it },
            onSendMessage = {
                if (message.isNotBlank()) {
                    viewModel.sendMessage(message)
                    message = ""
                }
            }
        )
    }
}