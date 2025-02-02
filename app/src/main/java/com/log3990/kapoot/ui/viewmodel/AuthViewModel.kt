package com.log3990.kapoot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.log3990.kapoot.data.repository.UserRepository
import com.log3990.kapoot.utils.SessionManager
import com.log3990.kapoot.utils.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * AuthViewModel manages the authentication state including sign‑in, sign‑up, and logout.
 *
 * It uses [UserRepository] for network calls and [SessionManager] to persist the session.
 */
class AuthViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // Expose the current user session. If the user is logged in, this holds the session data.
    private val _userSession = MutableStateFlow<UserSession?>(null)
    val userSession: StateFlow<UserSession?> = _userSession

    // Loading state to indicate ongoing network operations.
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    // Error messages (if any) during authentication.
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        // Observe session changes from the SessionManager and update the local state.
        viewModelScope.launch {
            sessionManager.sessionFlow.collect { session ->
                if (session.loggedIn) {
                    _userSession.value = session
                } else {
                    _userSession.value = null
                }
            }
        }
    }

    /**
     * Sign in a user using the provided credentials.
     *
     * On success, the session is saved persistently.
     */
    fun signIn(username: String, password: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val signInSuccess = userRepository.signIn(username, password)
                if (signInSuccess) {
                    // Save session persistently (you can extend this to include more details)
                    sessionManager.saveSession(username)
                } else {
                    _errorMessage.value = "Sign In Failed. Check credentials."
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown Error on Sign In"
            }
            _loading.value = false
        }
    }

    /**
     * Sign up a new user.
     *
     * If sign-up is successful, the user is automatically signed in and the session is saved.
     */
    fun signUp(username: String, password: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val response = userRepository.signUp(username, password)
                if (response.isSuccessful && response.body()?.user != null) {
                    // After a successful sign-up, sign in automatically.
                    val signInSuccess = userRepository.signIn(username, password)
                    if (signInSuccess) {
                        sessionManager.saveSession(username)
                    } else {
                        _errorMessage.value = "Sign in failed after sign up."
                    }
                } else {
                    _errorMessage.value = response.body()?.message ?: "Sign Up Failed"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown Error on Sign Up"
            }
            _loading.value = false
        }
    }

    /**
     * Logs out the current user.
     *
     * This method attempts to log out from the server, but always clears the session locally.
     */
    fun logout() {
        viewModelScope.launch {
            try {
                // Optionally, notify the server about logout.
                userRepository.logout(_userSession.value?.username ?: "")
            } catch (e: Exception) {
                // Even if the logout API fails, clear the session.
            } finally {
                sessionManager.clearSession()
            }
        }
    }

    /**
     * Clears any error message.
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
