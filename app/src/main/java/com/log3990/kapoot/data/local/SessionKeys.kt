// File: SessionKeys.kt
package com.log3990.kapoot.data.local

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object SessionKeys {
    val USERNAME = stringPreferencesKey("username")
    val LOGGED_IN = booleanPreferencesKey("logged_in")
    val USER_EMAIL = stringPreferencesKey("email")
    val USER_TOKEN = stringPreferencesKey("token")
    val LAST_LOGIN = longPreferencesKey("last_login")
}
