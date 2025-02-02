package com.log3990.kapoot.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.log3990.kapoot.data.repository.UserRepository
import com.log3990.kapoot.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val username: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    init {
        // Check if user is already logged in from DataStore
        viewModelScope.launch {
            sessionManager.isLoggedInFlow.collect { isLoggedIn ->
                if (isLoggedIn) {
                    sessionManager.userNameFlow.collect { username ->
                        if (username.isNotBlank()) {
                            _authState.value = AuthState.Success(username)
                        }
                    }
                }
            }
        }
    }

    fun signUp(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = userRepository.signUp(username, password)
                if (response.isSuccessful && response.body()?.user != null) {
                    // Optionally automatically sign in user
                    signIn(username, password)
                } else {
                    _authState.value = AuthState.Error(response.body()?.message ?: "Sign Up Failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun signIn(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val success = userRepository.signIn(username, password)
                if (success) {
                    // Save user session locally
                    sessionManager.saveSession(username)
                    _authState.value = AuthState.Success(username)
                } else {
                    _authState.value = AuthState.Error("Sign In Failed. Check credentials.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            val currentState = _authState.value
            if (currentState is AuthState.Success) {
                try {
                    userRepository.logout(currentState.username)
                } catch (_: Exception) {
                    // even if server call fails, attempt local cleanup
                }
                sessionManager.clearSession()
                _authState.value = AuthState.Idle
            }
        }
    }
}
