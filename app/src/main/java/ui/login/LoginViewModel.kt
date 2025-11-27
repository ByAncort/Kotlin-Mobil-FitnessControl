package ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import data.network.AuthPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class RoleResponse(
    val id: Int,
    val name: String,
    val permissions: List<String> = emptyList()
)


@Serializable
data class LoginResponse(
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
data class Links(
    val self: Link,
    @SerialName("validate-token")
    val validateToken: Link
)
@Serializable
data class Link(
    val href: String
)
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LoginState())
    val ui = _uiState.asStateFlow()

    private val baseUrl = AppConfig.getBaseUrl()+":9020"
    private val authDataStoreManager: AuthPreferencesManager = AuthPreferencesManager(application)



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
        return withContext(Dispatchers.IO) {
            try {
                val url = "$baseUrl/api/auth/login"
                val request = LoginRequest(username, password)

                val connection = URL(url).openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("accept", "*/*")
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 15000
                    readTimeout = 15000
                }

                val jsonInput = Json.encodeToString(LoginRequest.serializer(), request)
                connection.outputStream.use { os ->
                    os.write(jsonInput.toByteArray())
                    os.flush()
                }

                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val loginResponse = Json.decodeFromString<LoginResponse>(responseText)
                    saveAuthData(loginResponse)
                    loginResponse
                } else {
                    val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    throw Exception("Error $responseCode: $errorText")
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    private suspend fun saveAuthData(loginResponse: LoginResponse) {
        authDataStoreManager.saveAuthData(loginResponse)
    }
    fun checkLoginStatus() {
        viewModelScope.launch {
            val token = authDataStoreManager.authToken.first()
            if (!token.isNullOrEmpty()) {
                _uiState.update { it.copy(loggedIn = true) }
            }
        }
    }
    fun logout() {
        viewModelScope.launch {
                authDataStoreManager.clearAuthData()
                _uiState.update {
                    it.copy(
                        loggedIn = false,
                        username = "",
                        password = "",
                        message = "Sesión cerrada"
                    )
                }

        }
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