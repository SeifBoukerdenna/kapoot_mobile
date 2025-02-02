package com.log3990.kapoot.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.log3990.kapoot.socket.SocketClientService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    username: String,
    onLogout: () -> Unit
) {
    var message by remember { mutableStateOf("") }
    val logs = remember { mutableStateListOf<String>() }
    val scope = rememberCoroutineScope()
    val listState: LazyListState = rememberLazyListState()

    /**
     * Adds a message to the logs in a standardized format.
     *
     * - For system messages (sender == null): "[HH:mm] message"
     * - For user messages: "[HH:mm] user: message"
     */
    fun addMessageToLogs(time: String, message: String, sender: String? = null) {
        val formattedMessage = if (sender != null) {
            "[$time] $sender: $message"
        } else {
            "[$time] $message"
        }
        scope.launch(Dispatchers.Main) {
            Log.d("ChatScreen", "Adding message to logs: $formattedMessage")
            logs.add(0, formattedMessage)
            if (logs.size > 100) {
                logs.removeAt(logs.lastIndex)
            }
        }
    }

    // Returns current time formatted as HH:mm.
    fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    LaunchedEffect(username) {
        // Connect to the socket.
        SocketClientService.connect(
            username,
            onConnect = {
                Log.d("ChatScreen", "Connected to socket server")
                SocketClientService.joinGlobalRoom()
            },
            onError = { error ->
                Log.e("ChatScreen", "Socket connection error: ${error.message}")
                addMessageToLogs(getCurrentTime(), "Error: Failed to connect to chat server")
            }
        )

        // Listen for user chat updates.
        SocketClientService.on("updateChat") { args ->
            scope.launch(Dispatchers.Main) {
                Log.d("ChatScreen", "Received updateChat: ${args.joinToString()}")
                val (timeFromServer, payload) = args.map { it.toString() }.let { list ->
                    when (list.size) {
                        2 -> list[0] to list[1]
                        1 -> getCurrentTime() to list[0]
                        else -> getCurrentTime() to args.joinToString()
                    }
                }

                if (":" in payload) {
                    val (sender, messageContent) = payload.split(":", limit = 2).map { it.trim() }
                    addMessageToLogs(timeFromServer, messageContent, sender)
                } else {
                    addMessageToLogs(timeFromServer, payload)
                }
            }
        }

        // Listen for system messages.
        SocketClientService.on("message") { args ->
            scope.launch(Dispatchers.Main) {
                Log.d("ChatScreen", "Received system message: ${args.joinToString()}")
                if (args.isNotEmpty()) {
                    addMessageToLogs(getCurrentTime(), args[0].toString())
                }
            }
        }

        // Add an initial system message.
        addMessageToLogs(getCurrentTime(), "Connected to chat room")
    }

    // Clean up socket listeners when this composable is disposed.
    DisposableEffect(Unit) {
        onDispose {
            Log.d("ChatScreen", "Cleaning up socket listeners")
            SocketClientService.off("updateChat")
            SocketClientService.off("message")
            SocketClientService.disconnect()
        }
    }

    // Auto-scroll to the top (index 0) when a new message is added.
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        // Display the connected user's name.
        Text(
            text = "Logged in as: $username",
            style = TextStyle(fontSize = 18.sp),
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Messages list.
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5))
                .padding(8.dp)
        ) {
            items(logs) { log ->
                Text(
                    text = log,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth(),
                    color = Color.Black,
                    style = TextStyle(fontSize = 16.sp)
                )
            }
        }

        // Message input area.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.weight(1f),
                label = { Text("Enter message", color = Color.Black) },
                textStyle = TextStyle(color = Color.Black),
                maxLines = 3
            )
            Button(
                onClick = {
                    if (message.isNotBlank()) {
                        Log.d("ChatScreen", "Sending message: $message")
                        SocketClientService.sendMessage(message)
                        message = ""
                    }
                }
            ) {
                Text("Send", color = Color.White)
            }
        }

        // Logout button.
        Button(
            onClick = {
                Log.d("ChatScreen", "Logging out")
                SocketClientService.disconnect()
                onLogout()
            },
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
        ) {
            Text("Logout", color = Color.White)
        }
    }
}
