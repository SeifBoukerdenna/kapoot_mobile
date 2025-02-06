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
    fun processIncomingMessage(rawTime: String, payload: String, currentUsername: String): ChatMessage {
        val time = TimeUtils.getCurrentTime()

        // Handle array-formatted messages
        if (payload.startsWith("[") && payload.endsWith("]")) {
            try {
                val jsonArray = JSONArray(payload)
                if (jsonArray.length() >= 2) {
                    // Extract just the message part (second element)
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
                val username = content.substringBefore(" has")
                val event = content.substringAfter(username)
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

    private fun isSystemEvent(content: String): Boolean {
        return content.contains("has joined") ||
                content.contains("has left") ||
                content.contains("has disconnected")
    }

    fun sendMessage(message: String) {
        val cleanMessage = message.trim().replace(Regex("\\s+"), " ")
        if (cleanMessage.isNotEmpty()) {
            chatSocketManager.sendMessage(cleanMessage)
        }
    }
}