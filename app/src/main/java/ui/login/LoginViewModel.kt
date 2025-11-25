package ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URL

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class RoleResponse(
    val id: Int,
    val name: String,
    val permissions: List<String>
)

@Serializable
data class LoginResponse(
    val token: String,
    val tokenType: String,
    val issuedAt: String,
    val expiresAt: String,
    val username: String,
    val roles: List<RoleResponse>,
    val message: String?,
    val _links: Map<String, Map<String, String>>
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val ui = _uiState.asStateFlow()

    // Configuración de la API
    private val baseUrl = "http://192.168.100.22:9020/"

    fun onUsernameChange(username: String) {
        _uiState.update { currentState ->
            currentState.copy(
                username = username,
                error = null
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { currentState ->
            currentState.copy(
                password = password,
                error = null
            )
        }
    }

    fun submit() {
        val currentState = _uiState.value

        // Validaciones básicas
        if (currentState.username.isBlank()) {
            _uiState.update { it.copy(error = "El nombre de usuario es requerido") }
            return
        }

        if (currentState.password.isBlank()) {
            _uiState.update { it.copy(error = "La contraseña es requerida") }
            return
        }

        // Realizar login con la API
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }

            try {
                val loginResult = performLogin(currentState.username, currentState.password)

                if (loginResult != null) {
                    // Guardar el token (aquí puedes usar SharedPreferences, SecureStorage, etc.)
                    saveAuthToken(loginResult.token)

                    _uiState.update {
                        it.copy(
                            loading = false,
                            loggedIn = true,
                            message = "¡Login exitoso! Bienvenido ${loginResult.username}"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            loading = false,
                            error = "Error en el login"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = "Error de conexión: ${e.message ?: "Verifica tu conexión"}"
                    )
                }
            }
        }
    }

    private suspend fun performLogin(username: String, password: String): LoginResponse? {
        return try {
            val url = "$baseUrl/api/auth/login"
            val request = LoginRequest(username, password)

            // Usar ktor-client o HttpURLConnection para hacer la petición
            // Aquí un ejemplo básico con HttpURLConnection
            val connection = URL(url).openConnection() as java.net.HttpURLConnection
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("accept", "*/*")
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }

            // Convertir el request a JSON
            val jsonInput = Json.encodeToString(LoginRequest.serializer(), request)
            connection.outputStream.use { os ->
                os.write(jsonInput.toByteArray())
                os.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                Json.decodeFromString<LoginResponse>(responseText)
            } else {
                val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() }
                throw Exception("Error $responseCode: $errorText")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private fun saveAuthToken(token: String) {
        // Aquí guardas el token en SharedPreferences, SecureStorage, etc.
        // Ejemplo básico:
        // sharedPreferences.edit().putString("auth_token", token).apply()
        println("Token guardado: $token")
    }

    fun messageConsumed() {
        _uiState.update { it.copy(message = null) }
    }
}

data class LoginState(
    val username: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val loggedIn: Boolean = false
)