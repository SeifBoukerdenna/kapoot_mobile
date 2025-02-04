// File: LogoutUseCase.kt
package com.log3990.kapoot.domain.usecase.auth

import com.log3990.kapoot.data.repository.UserRepository
import com.log3990.kapoot.data.local.SessionManager
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(username: String) {
        try {
            userRepository.logout(username)
        } finally {
            sessionManager.clearSession()
        }
    }
}