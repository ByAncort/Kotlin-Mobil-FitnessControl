package data


import kotlinx.serialization.Serializable

@Serializable
data class Exercise(
    val name: String,
    val type: String,
    val muscle: String,
    val equipment: String? = null,
    val difficulty: String? = null,
    val instructions: String? = null
)