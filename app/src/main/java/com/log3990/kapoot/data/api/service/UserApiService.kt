// File: UserApiService.kt
package com.log3990.kapoot.data.api.service

import com.log3990.kapoot.data.api.ApiConstants
import com.log3990.kapoot.data.api.model.request.NewUserRequest
import com.log3990.kapoot.data.api.model.request.UpdateUserRequest
import com.log3990.kapoot.data.api.model.response.MessageResponse
import com.log3990.kapoot.data.api.model.response.UserResponse
import retrofit2.Response
import retrofit2.http.*

interface UserApiService {
    @POST(ApiConstants.USERS_ENDPOINT)
    suspend fun signUp(
        @Body newUser: NewUserRequest
    ): Response<UserResponse>

    @GET("${ApiConstants.USERS_ENDPOINT}/{username}")
    suspend fun getUserByUsername(
        @Path("username") username: String
    ): Response<MessageResponse>

    @PATCH("${ApiConstants.USERS_ENDPOINT}/{username}")
    suspend fun patchUserState(
        @Path("username") username: String,
        @Body request: UpdateUserRequest
    ): Response<Void>
}