package ui.login

import data.model.User
import data.repository.AuthRepository
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val loggedIn: Boolean = false,
    val user: User? = null,
    val message: String? = null   // para Snackbar one-shot (opcional)
)

class LoginViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui

    fun onEmailChange(v: String)    = _ui.update { it.copy(email = v, error = null, message = null) }
    fun onPasswordChange(v: String) = _ui.update { it.copy(password = v, error = null, message = null) }

    private fun validar(): String? {
        val s = _ui.value
        if (!Patterns.EMAIL_ADDRESS.matcher(s.email).matches()) return "Email inválido"
        if (s.password.length < 6) return "La clave debe tener al menos 6 caracteres"
        return null
    }

    fun submit() {
        val err = validar()
        if (err != null) {
            _ui.update { it.copy(error = err) }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null, message = null) }
            val user = repo.login(_ui.value.email, _ui.value.password)
            _ui.update {
                if (user != null) it.copy(loading = false, loggedIn = true, user = user, message = "Ingreso exitoso")
                else it.copy(loading = false, error = "Error al iniciar sesión")
            }
        }
    }

    fun messageConsumed() { _ui.update { it.copy(message = null) } }
}