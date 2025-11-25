package model.auth

data class LoginRequest(
    val username: String,
    val password: String
)