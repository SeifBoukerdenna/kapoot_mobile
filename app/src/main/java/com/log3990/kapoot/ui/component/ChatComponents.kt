package com.log3990.kapoot.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel


import com.log3990.kapoot.data.model.ChatMessage

@Composable
fun MessageList(
    messages: List<ChatMessage>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
        reverseLayout = true
    ) {
        items(messages) { message ->
            MessageBubble(message = message)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val isSystemMessage = message.sender == null

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        // Time and sender
        Text(
            text = buildString {
                append("[${message.time}] ")
                if (!isSystemMessage) {
                    append(message.sender)
                }
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Message content
        Surface(
            shape = RoundedCornerShape(
                topStart = if (message.sender == "Vous") 12.dp else 4.dp,
                topEnd = if (message.sender == "Vous") 4.dp else 12.dp,
                bottomStart = 12.dp,
                bottomEnd = 12.dp
            ),
            color = when {
                isSystemMessage -> MaterialTheme.colorScheme.surfaceVariant
                message.sender == "Vous" -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.secondaryContainer
            },
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(12.dp),
                color = if (isSystemMessage) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
            )
        }
    }
}

@Composable
fun MessageInputBar(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val maxChars = 200
    val remainingChars = maxChars - message.length
    val isNearLimit = remainingChars <= 20

    Surface(
        modifier = modifier,
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = message,
                    onValueChange = { if (it.length <= maxChars) onMessageChange(it) },
                    placeholder = { Text("Type a message") },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 40.dp),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    supportingText = if (isNearLimit) {
                        { Text("$remainingChars characters remaining") }
                    } else null
                )

                IconButton(
                    onClick = onSendMessage,
                    enabled = message.isNotBlank() && message.length <= maxChars,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (message.isNotBlank() && message.length <= maxChars) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send message",
                        tint = if (message.isNotBlank() && message.length <= maxChars) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // Character counter
            if (message.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = "${message.length}/$maxChars",
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            remainingChars <= 10 -> MaterialTheme.colorScheme.error
                            isNearLimit -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        }
    }
}