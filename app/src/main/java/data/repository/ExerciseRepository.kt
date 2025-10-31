package data.repository

import data.remote.model.Exercise
import data.remote.ExerciseService

class ExerciseRepository(private val exerciseService: ExerciseService) {

    suspend fun getAllExercises(): List<Exercise> {
        return exerciseService.getAllExercises()
    }

    suspend fun getExercisesByMuscle(muscle: String): List<Exercise> {
        return exerciseService.getExercisesByMuscle(muscle)
    }

    suspend fun getExercisesByType(type: String): List<Exercise> {
        return exerciseService.getExercisesByType(type)
    }
}

