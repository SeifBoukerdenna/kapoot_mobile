// File: UserRepository.kt
package com.log3990.kapoot.data.repository

import android.util.Log
import com.google.gson.Gson
import com.log3990.kapoot.data.api.model.request.NewUserRequest
import com.log3990.kapoot.data.api.model.request.UpdateUserRequest
import com.log3990.kapoot.data.api.service.UserApiService
import com.log3990.kapoot.data.model.User
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userApi: UserApiService,
    private val gson: Gson
) {
    suspend fun signUp(name: String, password: String): Result<User> {
        return try {
            val checkResponse = userApi.getUserByUsername(name)
            if (checkResponse.isSuccessful) {
                Result.failure(Exception("Username already in use"))
            } else {
                val userJsonString = """{"name":"$name","mdp":"$password"}"""
                val response = userApi.signUp(NewUserRequest("", userJsonString))
                if (response.isSuccessful && response.body()?.user != null) {
                    Result.success(response.body()!!.user!!)
                } else {
                    Result.failure(Exception(response.body()?.message ?: "Sign up failed"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(username: String, password: String): Result<Boolean> {
        return try {
            val response = userApi.getUserByUsername(username)
            if (!response.isSuccessful) {
                return Result.failure(Exception("User not found"))
            }

            val responseBody = response.body()?.body ?: return Result.failure(Exception("Empty response"))
            val user = gson.fromJson(responseBody, User::class.java)

            Log.d("UserRepository", "Parsed user: $user")

            // First verify that this is the correct user and password
            if (user.name != username || user.mdp != password) {
                return Result.failure(Exception("Invalid credentials"))
            }

            // Only after confirming it's the right user, check if they're already connected
            if (user.isConnected) {
                Log.d("UserRepository", "User is already connected")
                return Result.failure(Exception("User is already connected from another session"))
            }

            // If we get here, user exists, credentials are correct, and user isn't connected
            patchUserState(username, true)
            Result.success(true)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error in signIn: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun logout(username: String) {
        try {
            patchUserState(username, false)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error in logout: ${e.message}", e)
        }
    }

    private suspend fun patchUserState(username: String, isConnected: Boolean) {
        userApi.patchUserState(
            username,
            UpdateUserRequest(body = "{\"isConnected\":$isConnected}")
        )
    }
}