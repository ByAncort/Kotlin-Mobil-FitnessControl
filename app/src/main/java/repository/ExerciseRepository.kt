package repository

import data.Exercise

import service.ExerciseService

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

