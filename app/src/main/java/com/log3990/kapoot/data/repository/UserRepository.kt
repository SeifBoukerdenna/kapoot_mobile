// File: UserRepository.kt
package com.log3990.kapoot.data.repository

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
                    Result.success(response.body()!!.user!!) // Force non-null since we checked it
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

            val user = gson.fromJson(response.body()?.body, User::class.java)
            if (user.isConnected) {
                return Result.failure(Exception("User already connected from another session."))
            }

            if (user.mdp == password) {
                patchUserState(username, true)
                Result.success(true)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(username: String) {
        patchUserState(username, false)
    }

    private suspend fun patchUserState(username: String, isConnected: Boolean) {
        userApi.patchUserState(
            username,
            UpdateUserRequest(body = "{\"isConnected\":$isConnected}")
        )
    }
}
