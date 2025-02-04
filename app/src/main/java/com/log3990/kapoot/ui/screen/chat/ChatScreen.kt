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
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.log3990.kapoot.data.model.ChatMessage
import com.log3990.kapoot.ui.viewmodel.ChatViewModel

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

    ChatScreenContent(
        username = username,
        message = message,
        messages = messages,
        onMessageChange = { message = it },
        onSendMessage = {
            if (message.isNotBlank()) {
                viewModel.sendMessage(message)
                message = ""
            }
        },
        onLogout = {
            viewModel.disconnect()
            onLogout()
        }
    )
}

@Composable
private fun ChatScreenContent(
    username: String,
    message: String,
    messages: List<ChatMessage>,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        Text(
            text = "Logged in as: $username",
            style = TextStyle(fontSize = 18.sp),
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        MessageList(
            messages = messages,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        MessageInput(
            message = message,
            onMessageChange = onMessageChange,
            onSendMessage = onSendMessage
        )

        LogoutButton(onLogout = onLogout)
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
private fun MessageItem(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = buildString {
                append("[${message.time}] ")
                if (message.sender != null) {
                    append("${message.sender}: ")
                }
            },
            style = TextStyle(
                fontSize = 12.sp,
                color = Color.Gray
            )
        )
        Text(
            text = message.content,
            style = TextStyle(
                fontSize = 14.sp,
                color = Color.Black
            ),
            modifier = Modifier.padding(start = 8.dp)
        )
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


@Composable
private fun MessageInput(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = message,
            onValueChange = onMessageChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type a message") }
        )
        Button(onClick = onSendMessage) {
            Text("Send")
        }
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    Button(
        onClick = onLogout,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Logout")
    }
}