package data

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutRoutine(
    val id: Long? = null,
    val name: String,
    val description: String,
    val duration: String,
    val exercises: List<RoutineExercise> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
)