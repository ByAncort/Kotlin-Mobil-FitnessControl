package data

import kotlinx.serialization.Serializable

@Serializable
data class RoutineExercise(
    val id: Long? = null,
    val exercise: Exercise,
    val sets: Int,
    val reps: Int,
    val restTime: Int? = null
)