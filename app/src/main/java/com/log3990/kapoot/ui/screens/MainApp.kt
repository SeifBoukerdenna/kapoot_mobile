package com.log3990.kapoot.ui.screens

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.log3990.kapoot.data.repository.UserRepository
import com.log3990.kapoot.data.api.RetrofitInstance
import com.log3990.kapoot.utils.SessionManager
import com.log3990.kapoot.utils.UserSession
import kotlinx.coroutines.launch
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MainApp() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val sessionState by sessionManager.sessionFlow.collectAsState(
        initial = UserSession("", "", "", 0L, false)
    )
    val scope = rememberCoroutineScope()

    // Repository instance for sign in/up and logout operations.
    val userRepository = remember { UserRepository(RetrofitInstance.userApi) }

    // Local state for loading and error messages.
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isOnSignUpScreen by remember { mutableStateOf(false) }

    when {
        loading -> {
            CircularProgressIndicator()
        }
        errorMessage != null -> {
            // Display the error screen when there is an error message.
            ErrorScreen(message = errorMessage!!) {
                errorMessage = null // Reset the error on retry.
            }
        }
        else -> {
            if (sessionState.loggedIn) {
                // User is logged in: show ChatScreen.
                ChatScreen(
                    username = sessionState.username,
                    onLogout = {
                        scope.launch {
                            try {
                                userRepository.logout(sessionState.username)
                            } catch (e: Exception) {
                                // Even if logout fails on server, clear the session locally.
                            }
                            sessionManager.clearSession()
                        }
                    }
                )
            } else {
                if (isOnSignUpScreen) {
                    SignUpScreen(
                        onSignUpConfirmed = { username, password ->
                            loading = true
                            scope.launch {
                                try {
                                    // Call signUp. This call will throw an exception if the username exists.
                                    val response = userRepository.signUp(username, password)
                                    if (response.isSuccessful && response.body()?.user != null) {
                                        val signInSuccess = userRepository.signIn(username, password)
                                        if (signInSuccess) {
                                            // Save session persistently.
                                            sessionManager.saveSession(username)
                                        } else {
                                            errorMessage = "Sign in failed after sign up."
                                        }
                                    } else {
                                        errorMessage = response.body()?.message ?: "Sign Up Failed"
                                    }
                                } catch (e: Exception) {
                                    // This will catch exceptions like "Username already in use".
                                    errorMessage = e.message ?: "Unknown Error on Sign Up"
                                }
                                loading = false
                            }
                        },
                        onBackToSignIn = { isOnSignUpScreen = false }
                    )
                } else {
                    SignInScreen(
                        onSignInClicked = { username, password ->
                            loading = true
                            scope.launch {
                                try {
                                    val signInSuccess = userRepository.signIn(username, password)
                                    if (signInSuccess) {
                                        sessionManager.saveSession(username)
                                    } else {
                                        errorMessage = "Sign In Failed. Check credentials."
                                    }
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Unknown Error on Sign In"
                                }
                                loading = false
                            }
                        },
                        onSignUpNavigate = { isOnSignUpScreen = true }
                    )
                }
            }
        }
    }
}


@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = "Error: $message", color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

