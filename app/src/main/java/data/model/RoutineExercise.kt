package data.model

import data.remote.model.Exercise
import kotlinx.serialization.Serializable

@Serializable
data class RoutineExercise(
    val exercise: Exercise,
    val sets: Int,
    val reps: Int,
    val restTime: Int
)