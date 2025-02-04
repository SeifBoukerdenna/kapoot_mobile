// File: UserSession.kt
package com.log3990.kapoot.domain.model

data class UserSession(
    val username: String,
    val email: String,
    val token: String,
    val lastLogin: Long,
    val loggedIn: Boolean
)
