package ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import data.repository.AuthRepository
import data.repository.ProfileRepository

data class EditProfileUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val weight: String = "",
    val height: String = "",
    val photoUri: Uri? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
    val message: String? = null
)

class EditProfileViewModel(
    private val authRepo: AuthRepository = AuthRepository(),
    private val profileRepo: ProfileRepository = ProfileRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(EditProfileUiState())
    val ui: StateFlow<EditProfileUiState> = _ui

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val currentUser = authRepo.currentUser()
            if (currentUser?.uid != null) {
                val userProfile = profileRepo.getUserProfile(currentUser.uid)
                _ui.update { state ->
                    state.copy(
                        name = userProfile?.displayName ?: currentUser.displayName ?: "",
                        email = currentUser.email ?: "",
                        phone = userProfile?.phone ?: "",
                        weight = userProfile?.weight?.toString() ?: "",
                        height = userProfile?.height?.toString() ?: "",
                        photoUri = userProfile?.photoUri?.let { Uri.parse(it) }
                    )
                }
            }
        }
    }

    fun onNameChange(v: String) = _ui.update { it.copy(name = v, error = null) }
    fun onPhoneChange(v: String) = _ui.update { it.copy(phone = v, error = null) }
    fun onWeightChange(v: String) {
        if (v.isEmpty() || v.matches(Regex("^\\d*\\.?\\d*$"))) {
            _ui.update { it.copy(weight = v, error = null) }
        }
    }
    fun onHeightChange(v: String) {
        if (v.isEmpty() || v.matches(Regex("^\\d*$"))) {
            _ui.update { it.copy(height = v, error = null) }
        }
    }

    fun onPhotoSelected(uri: Uri) {
        _ui.update { it.copy(photoUri = uri, error = null) }
    }

    fun removePhoto() {
        _ui.update { it.copy(photoUri = null) }
    }

    private fun validate(): String? {
        val s = _ui.value

        if (s.name.isBlank()) {
            return "El nombre es obligatorio"
        }

        if (s.name.length < 2) {
            return "El nombre debe tener al menos 2 caracteres"
        }

        if (s.phone.isNotEmpty() && s.phone.length < 8) {
            return "Teléfono inválido"
        }

        if (s.weight.isNotEmpty()) {
            val weight = s.weight.toDoubleOrNull()
            if (weight == null || weight <= 0 || weight > 500) {
                return "Peso inválido (debe estar entre 0 y 500 kg)"
            }
        }

        if (s.height.isNotEmpty()) {
            val height = s.height.toIntOrNull()
            if (height == null || height <= 0 || height > 300) {
                return "Altura inválida (debe estar entre 0 y 300 cm)"
            }
        }

        return null
    }

    fun save() {
        val error = validate()
        if (error != null) {
            _ui.update { it.copy(error = error) }
            return
        }

        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }

            try {
                val currentUser = authRepo.currentUser()
                if (currentUser?.uid == null) {
                    _ui.update {
                        it.copy(
                            loading = false,
                            error = "Usuario no autenticado"
                        )
                    }
                    return@launch
                }

                val currentState = _ui.value

                // Subir foto si existe
                var photoUrl: String? = null
                if (currentState.photoUri != null) {
                    photoUrl = profileRepo.uploadProfilePhoto(
                        currentUser.uid,
                        currentState.photoUri
                    )
                }

                // Crear objeto de usuario actualizado
                val updatedUser = UserProfile(
                    uid = currentUser.uid,
                    email = currentState.email,
                    displayName = currentState.name,
                    phone = currentState.phone.ifBlank { null },
                    weight = currentState.weight.toDoubleOrNull(),
                    height = currentState.height.toIntOrNull(),
                    photoUri = photoUrl ?: currentState.photoUri?.toString()
                )

                val success = profileRepo.updateUserProfile(updatedUser)

                _ui.update {
                    if (success) {
                        it.copy(
                            loading = false,
                            saved = true,
                            message = "Perfil actualizado exitosamente"
                        )
                    } else {
                        it.copy(
                            loading = false,
                            error = "Error al actualizar el perfil"
                        )
                    }
                }
            } catch (e: Exception) {
                _ui.update {
                    it.copy(
                        loading = false,
                        error = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    fun showMessage(msg: String) {
        _ui.update { it.copy(message = msg) }
    }

    fun messageConsumed() {
        _ui.update { it.copy(message = null) }
    }
}

// Modelo extendido para el perfil
data class UserProfile(
    val uid: String,
    val email: String,
    val displayName: String? = null,
    val phone: String? = null,
    val weight: Double? = null,
    val height: Int? = null,
    val photoUri: String? = null
)