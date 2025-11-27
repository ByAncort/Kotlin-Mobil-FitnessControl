package data.network

import android.content.Context
import androidx.datastore.core.*
import androidx.datastore.preferences.*
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ui.login.LoginResponse

class AuthDataStoreManager(private val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_datastore")

    companion object {
        private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_EXPIRES_AT = stringPreferencesKey("expires_at")
    }

    suspend fun saveAuthData(loginResponse: LoginResponse) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTH_TOKEN] = loginResponse.token
            preferences[KEY_USERNAME] = loginResponse.username
            preferences[KEY_EXPIRES_AT] = loginResponse.expiresAt
        }
    }

    val authToken: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_AUTH_TOKEN]
        }

    val username: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_USERNAME]
        }
    suspend fun getToken(): String? {
        return context.dataStore.data.map { it[KEY_AUTH_TOKEN] }.first()
    }

    suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_AUTH_TOKEN)
            preferences.remove(KEY_USERNAME)
            preferences.remove(KEY_EXPIRES_AT)
        }
    }
}