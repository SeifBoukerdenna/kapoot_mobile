package com.log3990.kapoot.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.google.gson.Gson
import com.log3990.kapoot.data.model.User
import com.log3990.kapoot.data.repository.UserRepository
import com.log3990.kapoot.data.api.MessageResponse
import com.log3990.kapoot.data.api.RetrofitInstance
import kotlinx.coroutines.launch

@Composable
fun MainApp() {
    // Create repository instance and coroutine scope.
    val userRepository = remember { UserRepository(RetrofitInstance.userApi) }
    val scope = rememberCoroutineScope()

    // UI states.
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentUser by remember { mutableStateOf<User?>(null) }

    // Toggle for whether we're on the SignUp screen or SignIn screen.
    var isOnSignUpScreen by remember { mutableStateOf(false) }

    when {
        loading -> {
            CircularProgressIndicator()
        }
        errorMessage != null -> {
            ErrorScreen(message = errorMessage!!) {
                errorMessage = null
            }
        }
        // When the user is logged in, show the ChatScreen.
        currentUser != null -> {
            ChatScreen(
                username = currentUser!!.name,
                onLogout = {
                    scope.launch {
                        try {
                            userRepository.logout(currentUser!!.name)
                        } catch (e: Exception) {
                            // Even if the server call fails, we can still clear the local session.
                        }
                        currentUser = null
                    }
                }
            )
        }
        // If no user is logged in, show Sign In / Sign Up screens.
        else -> {
            if (isOnSignUpScreen) {
                SignUpScreen(
                    onSignUpConfirmed = { username, password ->
                        loading = true
                        scope.launch {
                            try {
                                val response = userRepository.signUp(username, password)
                                if (response.isSuccessful && response.body()?.user != null) {
                                    // Optionally sign in after successful sign up.
                                    val signInSuccess = userRepository.signIn(username, password)
                                    if (signInSuccess) {
                                        // Fetch the user object to show in the app.
                                        val getResp = userRepository.userApi.getUserByUsername(username)
                                        if (getResp.isSuccessful) {
                                            val messageResponse = getResp.body()
                                            if (messageResponse != null) {
                                                currentUser = Gson().fromJson(messageResponse.body, User::class.java)
                                            } else {
                                                errorMessage = "Failed to parse user after sign up."
                                            }
                                        } else {
                                            errorMessage = "Failed to fetch user after sign up."
                                        }
                                    } else {
                                        errorMessage = "Sign in failed after sign up."
                                    }
                                } else {
                                    errorMessage = response.body()?.message ?: "Sign Up Failed"
                                }
                            } catch (e: Exception) {
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
                                    // If sign in is successful, fetch the user from the server.
                                    val resp = userRepository.userApi.getUserByUsername(username)
                                    if (resp.isSuccessful) {
                                        val messageResponse = resp.body()
                                        if (messageResponse != null) {
                                            currentUser = Gson().fromJson(messageResponse.body, User::class.java)
                                        } else {
                                            errorMessage = "Failed to parse user after sign in."
                                        }
                                    } else {
                                        errorMessage = "Error fetching user after sign in."
                                    }
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

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Column {
        Text(text = "Error: $message")
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
