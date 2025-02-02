package com.log3990.kapoot.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "user_prefs"
val Context.userDataStore by preferencesDataStore(DATASTORE_NAME)

object SessionKeys {
    val USERNAME = stringPreferencesKey("username")
    val LOGGED_IN = booleanPreferencesKey("logged_in")
}

class SessionManager(private val context: Context) {

    val userNameFlow = context.userDataStore.data.map { prefs ->
        prefs[SessionKeys.USERNAME] ?: ""
    }

    val isLoggedInFlow = context.userDataStore.data.map { prefs ->
        prefs[SessionKeys.LOGGED_IN] ?: false
    }

    suspend fun saveSession(username: String) {
        context.userDataStore.edit { prefs ->
            prefs[SessionKeys.USERNAME] = username
            prefs[SessionKeys.LOGGED_IN] = true
        }
    }

    suspend fun clearSession() {
        context.userDataStore.edit { prefs ->
            prefs[SessionKeys.USERNAME] = ""
            prefs[SessionKeys.LOGGED_IN] = false
        }
    }
}
