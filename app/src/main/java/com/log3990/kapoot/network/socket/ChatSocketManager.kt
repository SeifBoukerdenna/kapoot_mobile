// ChatSocketManager.kt
package com.log3990.kapoot.network.socket

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import java.net.URISyntaxException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatSocketManager @Inject constructor() {
    private var socket: Socket? = null
    private val serverUrl = "http://164.90.131.87:3000"

    fun connect(
        username: String,
        onConnect: (() -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ) {
        try {
            Log.d(TAG, "Attempting to connect with username: $username")
            socket?.disconnect()

            val opts = IO.Options().apply {
                transports = arrayOf(WebSocket.NAME)
            }
            socket = IO.socket(serverUrl, opts)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket connected successfully")
                socket?.emit("setGlobalUsername", username)
                onConnect?.invoke()
            }

            socket?.on(Socket.EVENT_DISCONNECT) { args ->
                Log.d(TAG, "Socket disconnected. Args: ${args.contentToString()}")
            }

            // Register listener for other users' disconnections
            socket?.on("userDisconnected") { args ->
                if (args.isNotEmpty()) {
                    Log.d(TAG, "User disconnected: ${args[0]}")
                }
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val errorMsg = args.joinToString()
                Log.e(TAG, "Socket connection error: $errorMsg")
                if (args.isNotEmpty() && args[0] is Exception) {
                    onError?.invoke(args[0] as Exception)
                }
            }

            Log.d(TAG, "Initiating socket connection...")
            socket?.connect()
        } catch (e: URISyntaxException) {
            Log.e(TAG, "Socket URI error: ${e.message}")
            onError?.invoke(e)
        }
    }

    fun sendMessage(message: String) {
        Log.d(TAG, "Sending message: $message")
        socket?.emit("sendMessage", message)
    }

    fun joinGlobalRoom() {
        Log.d(TAG, "Joining global room")
        socket?.emit("connection")
    }

    fun on(event: String, listener: (Array<Any>) -> Unit) {
        Log.d(TAG, "Registering listener for event: $event")
        socket?.on(event) { args ->
            Log.d(TAG, "Event $event received with args: ${args.contentToString()}")
            listener(args)
        }
    }

    fun disconnect() {
        Log.d(TAG, "Disconnecting socket")
        // Send a message before disconnecting
        val lastMessage = "has left the chat"
        socket?.emit("sendDisconnectMessage", lastMessage)
        socket?.disconnect()
        socket = null
    }

    companion object {
        private const val TAG = "ChatSocketManager"
    }
}
