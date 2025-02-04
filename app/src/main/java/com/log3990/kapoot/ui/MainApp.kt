// File: MainApp.kt (Updated with ViewModels)
package com.log3990.kapoot.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.log3990.kapoot.ui.viewmodel.AuthViewModel
import com.log3990.kapoot.domain.model.UserSession
import com.log3990.kapoot.ui.screen.auth.SignInScreen
import com.log3990.kapoot.ui.screen.auth.SignUpScreen
import com.log3990.kapoot.ui.screen.chat.ChatScreen
import com.log3990.kapoot.ui.screen.common.ErrorScreen
import com.log3990.kapoot.ui.viewmodel.AuthUiState

@Composable
fun MainApp(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()

    when (val state = uiState) {
        is AuthUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is AuthUiState.Error -> {
            ErrorScreen(
                message = state.message,
                onRetry = { authViewModel.clearError() }
            )
        }
        is AuthUiState.Initial -> {
            AuthenticatedContent(
                authState = AuthUiState.Success(UserSession("", "", "", 0L, false)),
                onSignIn = authViewModel::signIn,
                onSignUp = authViewModel::signUp,
                onLogout = authViewModel::logout
            )
        }
        is AuthUiState.Success -> {
            AuthenticatedContent(
                authState = state,
                onSignIn = authViewModel::signIn,
                onSignUp = authViewModel::signUp,
                onLogout = authViewModel::logout
            )
        }
    }
}

@Composable
private fun AuthenticatedContent(
    authState: AuthUiState,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onLogout: (String) -> Unit
) {
    var isOnSignUpScreen by remember { mutableStateOf(false) }

    if (authState is AuthUiState.Success && authState.data.loggedIn) {
        ChatScreen(
            username = authState.data.username,
            onLogout = { onLogout(authState.data.username) }
        )
    } else {
        if (isOnSignUpScreen) {
            SignUpScreen(
                onSignUpConfirmed = onSignUp,
                onBackToSignIn = { isOnSignUpScreen = false }
            )
        } else {
            SignInScreen(
                onSignInClicked = onSignIn,
                onSignUpNavigate = { isOnSignUpScreen = true }
            )
        }
    }
}