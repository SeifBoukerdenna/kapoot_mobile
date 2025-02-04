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
            socket?.disconnect()  // Disconnect any existing connection

            val opts = IO.Options().apply {
                transports = arrayOf(WebSocket.NAME)
            }
            socket = IO.socket(serverUrl, opts)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket connected")
                onConnect?.invoke()
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e(TAG, "Socket connection error: ${args.joinToString()}")
                if (args.isNotEmpty() && args[0] is Exception) {
                    onError?.invoke(args[0] as Exception)
                }
            }

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
        socket?.on(event) { args ->
            Log.d(TAG, "Received $event event with args: ${args.joinToString()}")
            listener(args)
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
        Log.d(TAG, "Socket disconnected")
    }

    companion object {
        private const val TAG = "ChatSocketManager"
    }
}