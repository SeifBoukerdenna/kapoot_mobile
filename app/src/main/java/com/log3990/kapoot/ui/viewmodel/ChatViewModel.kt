// ChatViewModel.kt
package com.log3990.kapoot.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.log3990.kapoot.data.model.ChatMessage
import com.log3990.kapoot.domain.usecase.chat.ChatMessageUseCase
import com.log3990.kapoot.network.socket.ChatSocketManager
import com.log3990.kapoot.util.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatMessageUseCase: ChatMessageUseCase,
    private val chatSocketManager: ChatSocketManager
) : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private var currentUsername: String = ""

    fun connect(username: String) {
        // Clear previous messages when connecting
        _messages.value = emptyList()
        currentUsername = username

        chatSocketManager.connect(
            username = username,
            onConnect = {
                chatSocketManager.joinGlobalRoom()
                setupMessageListeners()
            }
        )
    }

    private fun setupMessageListeners() {
        // Listen for chat updates
        chatSocketManager.on("updateChat") { args ->
            viewModelScope.launch {
                if (args.isNotEmpty()) {
                    val newMessage = chatMessageUseCase.processIncomingMessage(
                        rawTime = "",
                        payload = args[0].toString(),
                        currentUsername = currentUsername
                    )
                    _messages.value = listOf(newMessage) + _messages.value
                }
            }
        }

        // Listen for system messages
        chatSocketManager.on("message") { args ->
            viewModelScope.launch {
                if (args.isNotEmpty()) {
                    val systemMessage = chatMessageUseCase.processIncomingMessage(
                        rawTime = "",
                        payload = args[0].toString(),
                        currentUsername = currentUsername
                    )
                    _messages.value = listOf(systemMessage) + _messages.value
                }
            }
        }

        // Listen for user disconnections
        chatSocketManager.on("disconnect") { args ->
            viewModelScope.launch {
                if (args.isNotEmpty()) {
                    val username = args[0].toString()
                    val disconnectMessage = ChatMessage.SystemEvent(
                        time = TimeUtils.getCurrentTime(),
                        username = username,
                        event = " has left the chat"
                    )
                    _messages.value = listOf(disconnectMessage) + _messages.value
                }
            }
        }
    }

    fun sendMessage(message: String) {
        chatMessageUseCase.sendMessage(message)
    }

    fun disconnect() {
        chatSocketManager.disconnect()
        // Clear messages when disconnecting
        _messages.value = emptyList()
        currentUsername = ""
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}