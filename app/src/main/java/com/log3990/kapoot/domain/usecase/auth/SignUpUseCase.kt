// File: SignUpUseCase.kt
package com.log3990.kapoot.domain.usecase.auth

import com.log3990.kapoot.data.repository.UserRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(username: String, password: String): Result<Unit> {
        return try {
            val signUpResult = userRepository.signUp(username, password)
            if (signUpResult.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(signUpResult.exceptionOrNull() ?: Exception("Sign up failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}