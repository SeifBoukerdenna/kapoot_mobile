// ChatMessageUseCase.kt
package com.log3990.kapoot.domain.usecase.chat

import android.util.Log
import com.log3990.kapoot.data.model.ChatMessage
import com.log3990.kapoot.network.socket.ChatSocketManager
import com.log3990.kapoot.util.TimeUtils
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONArray

@Singleton
class ChatMessageUseCase @Inject constructor(
    private val chatSocketManager: ChatSocketManager
) {
    private fun isSystemEvent(content: String): Boolean {
        return content.contains("has joined") ||
                content.contains("has left") ||
                content.contains("has disconnected") ||
                content.contains("has been disconnected")
    }

    fun processIncomingMessage(rawTime: String, payload: String, currentUsername: String): ChatMessage {
        val time = TimeUtils.getCurrentTime()

        // Handle array-formatted messages
        if (payload.startsWith("[") && payload.endsWith("]")) {
            try {
                val jsonArray = JSONArray(payload)
                if (jsonArray.length() >= 2) {
                    val actualMessage = jsonArray.getString(1)
                    return processMessageContent(time, actualMessage, currentUsername)
                }
            } catch (e: Exception) {
                Log.e("ChatMessageUseCase", "Error parsing array message: $e")
            }
        }

        return processMessageContent(time, payload, currentUsername)
    }

    private fun processMessageContent(time: String, content: String, currentUsername: String): ChatMessage {
        return when {
            isSystemEvent(content) -> {
                // Extract username and event from system message
                val username = when {
                    content.contains(" has ") -> content.substringBefore(" has ")
                    else -> "System"
                }
                val event = when {
                    content.contains(" has ") -> " has ${content.substringAfter(" has ")}"
                    else -> content
                }

                ChatMessage.SystemEvent(
                    time = time,
                    username = username,
                    event = event
                )
            }
            ":" in content -> {
                val (sender, message) = content.split(":", limit = 2)
                val cleanSender = sender.trim()
                val isSelf = cleanSender.equals(currentUsername, ignoreCase = true)

                ChatMessage.UserMessage(
                    time = time,
                    content = message.trim(),
                    sender = if (isSelf) "You" else cleanSender,
                    isSelf = isSelf
                )
            }
            else -> ChatMessage.UserMessage(
                time = time,
                content = content.trim(),
                sender = "System",
                isSelf = false
            )
        }
    }

    fun sendMessage(message: String) {
        val cleanMessage = message.trim().replace(Regex("\\s+"), " ")
        if (cleanMessage.isNotEmpty()) {
            chatSocketManager.sendMessage(cleanMessage)
        }
    }
}