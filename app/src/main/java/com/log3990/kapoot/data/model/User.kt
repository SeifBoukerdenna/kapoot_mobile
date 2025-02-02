package com.log3990.kapoot.data.model

data class User(
    val name: String,
    val mdp: String,
    val isConnected: Boolean = false
)
