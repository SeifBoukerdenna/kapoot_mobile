// File: AuthViewModel.kt
package com.log3990.kapoot.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.log3990.kapoot.data.repository.UserRepository
import com.log3990.kapoot.data.local.SessionManager
import com.log3990.kapoot.domain.model.UserSession
import com.log3990.kapoot.domain.usecase.auth.LogoutUseCase
import com.log3990.kapoot.domain.usecase.auth.SignInUseCase
import com.log3990.kapoot.domain.usecase.auth.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class AuthUiState {
    data object Initial : AuthUiState()
    data object Loading : AuthUiState()
    data class Success(val data: UserSession) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val uiState: StateFlow<AuthUiState> = _uiState

    init {
        viewModelScope.launch {
            sessionManager.sessionFlow.collect { session ->
                if (_uiState.value !is AuthUiState.Error) {
                    _uiState.value = AuthUiState.Success(session)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = AuthUiState.Initial
    }

    fun signIn(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val result = userRepository.signIn(username, password)
                when {
                    result.isSuccess && result.getOrNull() == true -> {
                        sessionManager.saveSession(username)
                    }
                    result.isFailure -> {
                        val error = result.exceptionOrNull()
                        Log.d("AuthViewModel", "Sign in failed: ${error?.message}")
                        _uiState.value = AuthUiState.Error(error?.message ?: "Unknown error")
                    }
                    else -> {
                        _uiState.value = AuthUiState.Error("Sign in failed")
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Sign in error: ${e.message}")
                _uiState.value = AuthUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun signUp(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val result = signUpUseCase(username, password)
                if (result.isSuccess) {
                    signIn(username, password)
                } else {
                    val error = result.exceptionOrNull()
                    Log.d("AuthViewModel", "Sign up failed: ${error?.message}")
                    _uiState.value = AuthUiState.Error(error?.message ?: "Sign up failed")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Sign up error: ${e.message}")
                _uiState.value = AuthUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun logout(username: String) {
        viewModelScope.launch {
            try {
                logoutUseCase(username)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Logout error: ${e.message}")
            }
        }
    }
}