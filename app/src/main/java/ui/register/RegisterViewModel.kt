package ui.register

import Model.User
import repository.Auth.AuthRepository
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirm: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val registered: Boolean = false,
    val user: User? = null,
    val message: String? = null
)

class RegisterViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(RegisterUiState())
    val ui: StateFlow<RegisterUiState> = _ui

    fun onEmailChange(v: String)    = _ui.update { it.copy(email = v, error = null, message = null) }
    fun onPasswordChange(v: String) = _ui.update { it.copy(password = v, error = null, message = null) }
    fun onConfirmChange(v: String)  = _ui.update { it.copy(confirm = v, error = null, message = null) }

    private fun validar(): String? {
        val s = _ui.value
        if (!Patterns.EMAIL_ADDRESS.matcher(s.email).matches()) return "Email inválido"
        if (s.password.length < 6) return "La clave debe tener al menos 6 caracteres"
        if (s.password != s.confirm) return "Las claves no coinciden"
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
            val user = repo.signUp(_ui.value.email, _ui.value.password)
            _ui.update {
                if (user != null) it.copy(loading = false, registered = true, user = user as User?, message = "Cuenta creada. Inicia sesión.")
                else it.copy(loading = false, error = "No se pudo crear la cuenta")
            }
        }
    }

    fun messageConsumed() { _ui.update { it.copy(message = null) } }
}