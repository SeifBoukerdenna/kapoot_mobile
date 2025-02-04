// File: UserResponse.kt
package com.log3990.kapoot.data.api.model.response

import com.log3990.kapoot.data.model.User

data class UserResponse(
    val message: String,
    val user: User?
)