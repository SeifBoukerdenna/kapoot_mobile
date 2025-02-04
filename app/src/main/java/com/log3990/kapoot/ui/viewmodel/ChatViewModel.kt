package com.log3990.kapoot.ui.viewmodel

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
        chatSocketManager.on("updateChat") { args ->
            viewModelScope.launch {
                if (args.isEmpty()) return@launch

                val messageList = _messages.value.toMutableList()
                val time = TimeUtils.getCurrentTime()
                val payload = args[0].toString()

                // Use the ChatMessageUseCase to process the message
                val newMessage = chatMessageUseCase.processIncomingMessage(
                    rawTime = time,
                    payload = payload,
                    currentUsername = currentUsername
                )

                messageList.add(0, newMessage)
                _messages.value = messageList
            }
        }
    }

    fun sendMessage(message: String) {
        // Use the ChatMessageUseCase to send the message
        chatMessageUseCase.sendMessage(message)
    }

    fun disconnect() {
        chatSocketManager.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
        chatSocketManager.disconnect()
    }
}