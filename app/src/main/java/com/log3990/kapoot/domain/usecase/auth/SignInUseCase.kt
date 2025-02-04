// File: SignInUseCase.kt
package com.log3990.kapoot.domain.usecase.auth

import com.log3990.kapoot.data.repository.UserRepository
import com.log3990.kapoot.data.local.SessionManager
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(username: String, password: String): Boolean {
        val result = userRepository.signIn(username, password)
        return when {
            result.isSuccess && result.getOrNull() == true -> {
                sessionManager.saveSession(username)
                true
            }
            else -> throw result.exceptionOrNull() ?: Exception("Sign in failed")
        }
    }
}
