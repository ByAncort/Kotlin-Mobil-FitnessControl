package model.auth

data class Role(
    val id: Long,
    val name: String,
    val permissions: Set<Permission>
)
