package com.log3990.kapoot.data.api

import com.log3990.kapoot.data.model.User
import retrofit2.Response
import retrofit2.http.*

interface UserApi {

    // SIGN UP: server expects { "title": "", "body": "<JSON string>" }
    @POST("api/client/users")
    suspend fun signUp(
        @Body newUser: NewUserRequest
    ): Response<UserResponse>

    // GET user by username. Server returns a message object with a JSON string in `body`
    @GET("api/client/users/{username}")
    suspend fun getUserByUsername(
        @Path("username") username: String
    ): Response<MessageResponse>

    // PATCH to update user state (e.g., isConnected)
    @PATCH("api/client/users/{username}")
    suspend fun patchUserState(
        @Path("username") username: String,
        @Body request: UpdateUserRequest
    ): Response<Void>
}

// ─────────────────────────────────────────────────────────────
// Data classes
// ─────────────────────────────────────────────────────────────

/**
 * For sign up, the server expects a top-level object with "title" and "body".
 * "body" must be a JSON-string containing the actual user data.
 */
data class NewUserRequest(
    val title: String = "",
    val body: String
)

/**
 * The response from the sign up route.
 */
data class UserResponse(
    val message: String,
    val user: User?
)

/**
 * For patch requests, the server expects an object like:
 * { "body": "{\"isConnected\": true}" }
 */
data class UpdateUserRequest(
    val body: String
)

/**
 * For GET requests, the server wraps the user in a message:
 * { "title": "", "body": "<stringified JSON>" }
 */
data class MessageResponse(
    val title: String,
    val body: String
)
