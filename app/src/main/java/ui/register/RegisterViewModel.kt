package ui.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import data.network.AuthPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ui.AppConfig
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class RegisterResponse(
    val token: String,
    @SerialName("tokenType")
    val tokenType: String,
    @SerialName("issuedAt")
    val issuedAt: String,
    @SerialName("expiresAt")
    val expiresAt: String,
    val username: String,
    val roles: List<RoleResponse>,
    val message: String? = null,
    @SerialName("_links")
    val links: Links? = null
)

@Serializable
data class RoleResponse(
    val id: Int,
    val name: String,
    val permissions: List<String> = emptyList()
)

@Serializable
data class Links(
    val self: Link? = null,                    // Hacer opcional
    @SerialName("validate-token")
    val validateToken: Link? = null,           // Hacer opcional
    val login: Link? = null                    // Hacer opcional
)

@Serializable
data class Link(
    val href: String
)

data class RegisterState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val registered: Boolean = false
)

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(RegisterState())
    val uiState = _uiState.asStateFlow()
    private val baseUrl = AppConfig.getBaseUrl()+":9020"

    private val authDataStoreManager: AuthPreferencesManager = AuthPreferencesManager(application)

    // Configuración del JSON para ignorar keys desconocidas
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun onUsernameChange(username: String) {
        _uiState.update { currentState ->
            currentState.copy(
                username = username,
                error = null
            )
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update { currentState ->
            currentState.copy(
                email = email,
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

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { currentState ->
            currentState.copy(
                confirmPassword = confirmPassword,
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

        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(error = "El email es requerido") }
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            _uiState.update { it.copy(error = "Email inválido") }
            return
        }

        if (currentState.password.isBlank()) {
            _uiState.update { it.copy(error = "La contraseña es requerida") }
            return
        }

        if (currentState.password.length < 6) {
            _uiState.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres") }
            return
        }

        if (currentState.password != currentState.confirmPassword) {
            _uiState.update { it.copy(error = "Las contraseñas no coinciden") }
            return
        }

        // Realizar registro con la API
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }

            try {
                val registerResult = performRegister(
                    currentState.username,
                    currentState.email,
                    currentState.password
                )

                if (registerResult != null) {
                    _uiState.update {
                        it.copy(
                            loading = false,
                            registered = true,
                            message = "¡Registro exitoso! Bienvenido ${registerResult.username}"
                        )
                    }
                    // Guardar los datos de autenticación automáticamente
                    saveAuthData(registerResult)
                } else {
                    _uiState.update {
                        it.copy(
                            loading = false,
                            error = "Error en el registro"
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

    private suspend fun performRegister(username: String, email: String, password: String): RegisterResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "$baseUrl/api/auth/register"
                val request = RegisterRequest(username, email, password)

                val connection = URL(url).openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("accept", "*/*")
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 15000
                    readTimeout = 15000
                }

                val jsonInput = json.encodeToString(RegisterRequest.serializer(), request)
                connection.outputStream.use { os ->
                    os.write(jsonInput.toByteArray())
                    os.flush()
                }

                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    println("✅ Respuesta del registro: $responseText") // Para debugging

                    val registerResponse = json.decodeFromString<RegisterResponse>(responseText)
                    registerResponse
                } else if (responseCode == 409) {
                    val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    throw Exception("El usuario ya existe")
                } else {
                    val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    throw Exception("Error $responseCode: $errorText")
                }
            } catch (e: Exception) {
                println("❌ Error en registro: ${e.message}") // Para debugging
                throw e
            }
        }
    }

    private suspend fun saveAuthData(registerResponse: RegisterResponse) {
        authDataStoreManager.saveAuthData(registerResponse)
    }

    fun messageConsumed() {
        _uiState.update { it.copy(message = null) }
    }

    fun clearState() {
        _uiState.update {
            RegisterState(
                registered = _uiState.value.registered // Mantener el estado de registro
            )
        }
    }
}