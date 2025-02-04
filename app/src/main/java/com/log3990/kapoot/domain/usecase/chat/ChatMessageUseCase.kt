// File: ChatMessageUseCase.kt
package com.log3990.kapoot.domain.usecase.chat

import com.log3990.kapoot.data.model.ChatMessage
import com.log3990.kapoot.network.socket.ChatSocketManager
import com.log3990.kapoot.util.TimeUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatMessageUseCase @Inject constructor(
    private val chatSocketManager: ChatSocketManager
) {
    fun sendMessage(message: String) {
        chatSocketManager.sendMessage(message)
    }

    fun processIncomingMessage(rawTime: String, payload: String, currentUsername: String): ChatMessage {
        val processedTime = TimeUtils.processTime(rawTime)

        return if (":" in payload) {
            val parts = payload.split(":")
            val sender = parts.first().trim().let {
                if (it.equals(currentUsername, ignoreCase = true)) "Vous" else it
            }
            val content = parts.drop(1).joinToString(":").trim()
            ChatMessage(processedTime, content, sender)
        } else {
            ChatMessage(processedTime, payload, null)
        }
    }
}