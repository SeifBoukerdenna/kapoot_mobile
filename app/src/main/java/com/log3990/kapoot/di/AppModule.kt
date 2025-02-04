// File: AppModule.kt
package com.log3990.kapoot.di

import android.content.Context
import com.google.gson.Gson
import com.log3990.kapoot.data.api.service.RetrofitService
import com.log3990.kapoot.data.api.service.UserApiService
import com.log3990.kapoot.data.repository.UserRepository
import com.log3990.kapoot.data.local.SessionManager
import com.log3990.kapoot.network.socket.ChatSocketManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.text.Typography.dagger

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideUserApiService(): UserApiService = RetrofitService.userApi

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideSessionManager(
        @ApplicationContext context: Context
    ): SessionManager = SessionManager(context)

    @Provides
    @Singleton
    fun provideChatSocketManager(): ChatSocketManager = ChatSocketManager()

    @Provides
    @Singleton
    fun provideUserRepository(
        userApi: UserApiService,
        gson: Gson
    ): UserRepository = UserRepository(userApi, gson)
}