package com.log3990.kapoot.data.repository

import com.google.gson.Gson
import com.log3990.kapoot.data.api.NewUserRequest
import com.log3990.kapoot.data.api.UpdateUserRequest
import com.log3990.kapoot.data.api.MessageResponse
import com.log3990.kapoot.data.api.UserApi
import com.log3990.kapoot.data.api.UserResponse
import com.log3990.kapoot.data.model.User
import retrofit2.Response

class UserRepository(val userApi: UserApi) {

    /**
     * Sign up user.
     * The server expects:
     * {
     *   "title": "",
     *   "body": "{\"name\":\"bob\",\"mdp\":\"1234\"}"
     * }
     */
    suspend fun signUp(name: String, password: String): Response<UserResponse> {
        val userJsonString = """{"name":"$name","mdp":"$password"}"""
        val newUserRequest = NewUserRequest(
            title = "",
            body = userJsonString
        )
        return userApi.signUp(newUserRequest)
    }

    /**
     * "Fake" sign in logic:
     *  1) GET /api/client/users/:username, which returns a MessageResponse.
     *  2) Parse the 'body' field (a JSON string) into a User object.
     *  3) Compare user.mdp with the provided password.
     *  4) If they match, call patchUserState to set isConnected = true.
     */
    suspend fun signIn(username: String, password: String): Boolean {
        val response = userApi.getUserByUsername(username)
        if (!response.isSuccessful) return false

        val messageResponse: MessageResponse = response.body() ?: return false

        // Parse the "body" string to a User object using Gson.
        val user = try {
            Gson().fromJson(messageResponse.body, User::class.java)
        } catch (e: Exception) {
            return false
        }

        return if (user.mdp == password) {
            patchUserState(username, isConnected = true)
            true
        } else {
            false
        }
    }

    /**
     * "Logout" logic: simply set isConnected to false.
     */
    suspend fun logout(username: String) {
        patchUserState(username, isConnected = false)
    }

    /**
     * Helper function to call the PATCH endpoint.
     * It sends a JSON string in the "body" field.
     */
    private suspend fun patchUserState(username: String, isConnected: Boolean) {
        val bodyJson = """{"isConnected":$isConnected}"""
        val updateRequest = UpdateUserRequest(
            body = """{"body":"$bodyJson"}"""
        )
        userApi.patchUserState(username, updateRequest)
    }
}
