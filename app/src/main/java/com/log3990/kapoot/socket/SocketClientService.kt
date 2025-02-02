package com.log3990.kapoot.socket

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.WebSocket
import java.net.URISyntaxException

object SocketClientService {
    private const val TAG = "SocketClientService"
    private const val SERVER_URL = "http://164.90.131.87:3000"
    private var socket: Socket? = null

    fun connect(
        username: String,
        onConnect: (() -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ) {
        try {
            val opts = IO.Options().apply {
                transports = arrayOf(WebSocket.NAME)
            }
            socket = IO.socket(SERVER_URL, opts)

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

    fun disconnect() {
        socket?.disconnect()
        socket = null
        Log.d(TAG, "Socket disconnected")
    }

    fun joinGlobalRoom() {
        socket?.let {
            if (it.connected()) {
                Log.d(TAG, "Joining global room...")
                it.emit("connection") // Emit initial connection event
                // Note: The server will automatically add us to the global room
            } else {
                Log.e(TAG, "Socket not connected. Cannot join global room.")
            }
        }
    }

    fun sendMessage(message: String) {
        socket?.let {
            if (it.connected()) {
                Log.d(TAG, "Sending message to global room: $message")
                it.emit("sendMessage", message)
            } else {
                Log.e(TAG, "Socket not connected. Cannot send message.")
            }
        }
    }

    fun on(event: String, listener: (args: Array<Any>) -> Unit) {
        Log.d(TAG, "Registering listener for event: $event")
        socket?.on(event) { args ->
            Log.d(TAG, "Received $event event with args: ${args.joinToString()}")
            listener(args)
        }
    }

    fun off(event: String) {
        socket?.off(event)
        Log.d(TAG, "Removed listener for event: $event")
    }
}