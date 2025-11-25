package data.network

data class AuthResponse(
    val token: String,
    val tokenType: String,
    val username: String,
    val message: String?,
    val roles: List<RoleDto>? // <--- Agrega esto
)
data class RoleDto(
    val id: Long,
    val name: String
    // Omitimos permissions si no los necesitas en el login
)