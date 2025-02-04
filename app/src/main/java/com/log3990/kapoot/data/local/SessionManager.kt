package com.log3990.kapoot.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.log3990.kapoot.domain.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userDataStore by preferencesDataStore("user_prefs")


@Singleton
class SessionManager @Inject constructor(
    private val context: Context
) {
    // Flow for the complete session state.
    val sessionFlow: Flow<UserSession> = context.userDataStore.data.map { prefs ->
        UserSession(
            username = prefs[SessionKeys.USERNAME] ?: "",
            email = prefs[SessionKeys.USER_EMAIL] ?: "",
            token = prefs[SessionKeys.USER_TOKEN] ?: "",
            lastLogin = prefs[SessionKeys.LAST_LOGIN] ?: 0L,
            loggedIn = prefs[SessionKeys.LOGGED_IN] ?: false
        )
    }

    /**
     * Saves the session with full details.
     *
     * @param username The username of the logged-in user.
     * @param email The email of the user.
     * @param token An authentication token (if applicable).
     */
    suspend fun saveSession(username: String, email: String, token: String) {
        context.userDataStore.edit { prefs ->
            prefs[SessionKeys.USERNAME] = username
            prefs[SessionKeys.USER_EMAIL] = email
            prefs[SessionKeys.USER_TOKEN] = token
            prefs[SessionKeys.LOGGED_IN] = true
            prefs[SessionKeys.LAST_LOGIN] = System.currentTimeMillis()
        }
    }

    /**
     * A simplified version to save only the username.
     */
    suspend fun saveSession(username: String) {
        context.userDataStore.edit { prefs ->
            prefs[SessionKeys.USERNAME] = username
            prefs[SessionKeys.LOGGED_IN] = true
            prefs[SessionKeys.LAST_LOGIN] = System.currentTimeMillis()
        }
    }

    /**
     * Clears all session data.
     */
    suspend fun clearSession() {
        context.userDataStore.edit { prefs ->
            prefs[SessionKeys.USERNAME] = ""
            prefs[SessionKeys.USER_EMAIL] = ""
            prefs[SessionKeys.USER_TOKEN] = ""
            prefs[SessionKeys.LOGGED_IN] = false
            prefs[SessionKeys.LAST_LOGIN] = 0L
        }
    }
}