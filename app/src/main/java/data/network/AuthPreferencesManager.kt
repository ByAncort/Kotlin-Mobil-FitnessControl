// data/network/AuthPreferencesManager.kt
package data.network

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ui.login.LoginResponse
import ui.register.RegisterResponse

class AuthPreferencesManager(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
            name = "auth_datastore"
        )
    }

    private val dataStore: DataStore<Preferences> = context.dataStore

    // Keys para almacenar los datos
    private object PreferencesKeys {
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USERNAME = stringPreferencesKey("username")
        val TOKEN_TYPE = stringPreferencesKey("token_type")
        val EXPIRES_AT = stringPreferencesKey("expires_at")
        val ISSUED_AT = stringPreferencesKey("issued_at")
        val ROLES = stringPreferencesKey("roles") // Almacenaremos los roles como JSON
    }

    // Método para guardar datos de LoginResponse
    suspend fun saveAuthData(loginResponse: LoginResponse) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTH_TOKEN] = loginResponse.token
            preferences[PreferencesKeys.USERNAME] = loginResponse.username
            preferences[PreferencesKeys.TOKEN_TYPE] = loginResponse.tokenType
            preferences[PreferencesKeys.EXPIRES_AT] = loginResponse.expiresAt
            preferences[PreferencesKeys.ISSUED_AT] = loginResponse.issuedAt

            // Convertir roles a JSON para almacenarlos
            val rolesJson = Json.encodeToString(loginResponse.roles)
            preferences[PreferencesKeys.ROLES] = rolesJson
        }
    }

    // Método para guardar datos de RegisterResponse
    suspend fun saveAuthData(registerResponse: RegisterResponse) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTH_TOKEN] = registerResponse.token
            preferences[PreferencesKeys.USERNAME] = registerResponse.username
            preferences[PreferencesKeys.TOKEN_TYPE] = registerResponse.tokenType
            preferences[PreferencesKeys.EXPIRES_AT] = registerResponse.expiresAt
            preferences[PreferencesKeys.ISSUED_AT] = registerResponse.issuedAt

            // Convertir roles a JSON para almacenarlos
            val rolesJson = Json.encodeToString(registerResponse.roles)
            preferences[PreferencesKeys.ROLES] = rolesJson
        }
    }

    // Método genérico que acepta cualquier tipo de respuesta de autenticación
    suspend fun saveAuthData(authResponse: Any) {
        when (authResponse) {
            is LoginResponse -> saveAuthData(authResponse)
            is RegisterResponse -> saveAuthData(authResponse)
            else -> throw IllegalArgumentException("Tipo de respuesta de autenticación no soportado")
        }
    }

    // Método para obtener el token
    suspend fun getToken(): String? {
        return dataStore.data.map { preferences ->
            preferences[PreferencesKeys.AUTH_TOKEN]
        }.firstOrNull()
    }

    // Método alternativo como Flow
    val authToken: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.AUTH_TOKEN]
        }

    // Obtener username
    val username: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USERNAME]
        }

    // Obtener token type
    val tokenType: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.TOKEN_TYPE]
        }

    // Obtener expires at
    val expiresAt: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.EXPIRES_AT]
        }

    // Obtener issued at
    val issuedAt: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ISSUED_AT]
        }

    // Guardar token individualmente
    suspend fun saveAuthToken(token: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTH_TOKEN] = token
        }
    }

    // Guardar username
    suspend fun saveUsername(username: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USERNAME] = username
        }
    }

    // Guardar ID de usuario
    suspend fun saveUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = userId
        }
    }

    // Obtener ID de usuario
    val userId: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_ID]
        }

    // Guardar email de usuario
    suspend fun saveUserEmail(email: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_EMAIL] = email
        }
    }

    // Obtener email de usuario
    val userEmail: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_EMAIL]
        }

    // Limpiar todos los datos (logout)
    suspend fun clearAuthData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // Verificar si el usuario está autenticado
    val isAuthenticated: Flow<Boolean> = dataStore.data
        .map { preferences ->
            !preferences[PreferencesKeys.AUTH_TOKEN].isNullOrEmpty()
        }
}