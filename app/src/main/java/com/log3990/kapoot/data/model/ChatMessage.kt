// ChatMessage.kt
package com.log3990.kapoot.data.model

sealed class ChatMessage {
    abstract val time: String

    data class UserMessage(
        override val time: String,
        val content: String,
        val sender: String,
        val isSelf: Boolean
    ) : ChatMessage()

    data class SystemEvent(
        override val time: String,
        val username: String,
        val event: String
    ) : ChatMessage()
}