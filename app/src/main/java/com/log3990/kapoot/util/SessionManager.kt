package com.log3990.kapoot.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "user_prefs"
val Context.userDataStore by preferencesDataStore(DATASTORE_NAME)

object SessionKeys {
    val USERNAME = stringPreferencesKey("username")
    val LOGGED_IN = booleanPreferencesKey("logged_in")
    val USER_EMAIL = stringPreferencesKey("email")
    val USER_TOKEN = stringPreferencesKey("token")
    val LAST_LOGIN = longPreferencesKey("last_login")
}

data class UserSession(
    val username: String,
    val email: String,
    val token: String,
    val lastLogin: Long,
    val loggedIn: Boolean
)

class SessionManager(private val context: Context) {

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
