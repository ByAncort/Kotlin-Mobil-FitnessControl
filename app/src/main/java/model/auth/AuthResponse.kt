package model.auth

import com.google.gson.annotations.SerializedName
import java.util.Date

data class AuthResponse(
    val token: String? = null,
    val tokenType: String? = null,
    @SerializedName("issuedAt")
    val issuedAt: Date? = null,
    @SerializedName("expiresAt")
    val expiresAt: Date? = null,
    val username: String? = null,
    val roles: Set<Role>? = null,
    val message: String? = null
)