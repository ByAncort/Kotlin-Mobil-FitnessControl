package data


import kotlinx.serialization.Serializable

@Serializable
data class Exercise(
    val name: String,
    val type: String, // category name
    val muscle: String, // primer músculo
    val equipment: String, // category name (barbell, dumbbell, etc)
    val difficulty: String,
    val instructions: String = ""
)