package data

import kotlinx.serialization.Serializable

@Serializable
data class Exercise(
    val id: Long? = null,
    val name: String,
    val type: String,
    val muscle: String,
    val equipment: String,
    val difficulty: String,
    val instructions: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)