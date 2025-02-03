package com.log3990.kapoot.data.repository

import com.google.gson.Gson
import com.log3990.kapoot.data.api.NewUserRequest
import com.log3990.kapoot.data.api.UpdateUserRequest
import com.log3990.kapoot.data.api.MessageResponse
import com.log3990.kapoot.data.api.UserApi
import com.log3990.kapoot.data.api.UserResponse
import com.log3990.kapoot.data.model.User
import retrofit2.Response

class UserRepository(private val userApi: UserApi) {


    /**
     * Signs up a new user.
     *
     * Before calling the sign up endpoint, we first check if the username is already in use.
     * If the GET user by username call returns a successful response, we throw an exception with
     * an error message indicating the username is already taken.
     */
    suspend fun signUp(name: String, password: String): Response<UserResponse> {
        // Check if the username is already taken.
        val checkResponse = userApi.getUserByUsername(name)
        if (checkResponse.isSuccessful) {
            // A successful response indicates the user exists, so we reject the sign-up.
            throw Exception("Username already in use")
        }

        // If checkResponse was not successful (e.g., 404 Not Found), proceed with sign up.
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

        // Prevent login if the user is already connected.
        if (user.isConnected) {
            throw Exception("User already connected from another session.")
        }

        return if (user.mdp == password) {
            // Set isConnected to true in the remote database.
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
        // Construct the JSON string exactly as the server expects.
        val updateRequest = UpdateUserRequest(
            body = "{\"isConnected\":$isConnected}"
        )
        userApi.patchUserState(username, updateRequest)
    }
}
