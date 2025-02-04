// File: ChatScreen.kt (Updated with ViewModel)
package com.log3990.kapoot.ui.screen.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.log3990.kapoot.data.model.ChatMessage
import com.log3990.kapoot.ui.component.MessageInputBar
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
                        imageVector = Icons.Filled.Edit,
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
                        imageVector = Icons.Filled.Close,
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



@Composable
private fun MessageList(
    messages: List<ChatMessage>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(8.dp),
        reverseLayout = true  // Most recent messages at the bottom
    ) {
        items(messages) { message ->
            MessageItem(message)
        }
    }
}


@Composable
private fun MessageItem(message: ChatMessage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "${message.sender ?: "System"} (${message.time})",
            style = TextStyle(fontSize = 12.sp, color = Color.Gray)
        )
        Text(
            text = message.content,
            style = TextStyle(fontSize = 14.sp)
        )
    }
}

