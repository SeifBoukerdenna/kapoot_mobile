package com.log3990.kapoot.ui.screens

import android.text.Layout
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

    // Helper function to add a message to the logs
    fun addMessageToLogs(time: String, chatMsg: String) {
        scope.launch(Dispatchers.Main) {
            Log.d("ChatScreen", "Adding message to logs: [$time] $chatMsg")
            logs.add(0, "[$time] $chatMsg")
            if (logs.size > 100) {
                logs.removeAt(logs.lastIndex)
            }
        }
    }

    // Function to get current time
    fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    LaunchedEffect(username) {
        // Connect socket
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

        // Listen for chat updates
        SocketClientService.on("updateChat") { args ->
            Log.d("ChatScreen", "Received updateChat: ${args.joinToString()}")
//            addMessageToLogs(getCurrentTime(), "Received updateChat")
            val info_back = listOf(args.joinToString())
            val (sender, message) = info_back[0].split(":", limit = 2)
            addMessageToLogs(getCurrentTime(), message)
            if (args.size >= 2) {
                val time = args[0].toString()
                val chatMsg = args[1].toString()
                addMessageToLogs(time, chatMsg)
            }
        }

        // Listen for system messages
        SocketClientService.on("message") { args ->
            Log.d("ChatScreen", "Received system message: ${args.joinToString()}")
            if (args.isNotEmpty()) {
                addMessageToLogs(getCurrentTime(), args[0].toString())
            }
        }

        // Add initial connection message
        addMessageToLogs(getCurrentTime(), "Connected to chat room")
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d("ChatScreen", "Cleaning up socket listeners")
            SocketClientService.off("updateChat")
            SocketClientService.off("message")
            SocketClientService.disconnect()
        }
    }

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
        // Messages list
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

        // Message input
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
                        // For immediate feedback, we can also add the message locally
                        addMessageToLogs(getCurrentTime(), "$username: $message")
                        message = ""
                    }
                },
            ) {
                Text("Send", color = Color.White)
            }
        }

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