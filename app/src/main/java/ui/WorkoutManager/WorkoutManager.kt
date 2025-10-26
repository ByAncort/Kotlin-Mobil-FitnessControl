package repository.workout

import android.content.Context
import data.AppDatabase
import data.local.entity.ActiveWorkoutEntity

class WorkoutManager(context: Context) {

    private val workoutDao = AppDatabase.getDatabase(context).activeWorkoutDao()

    /**
     * Inicia un nuevo entrenamiento
     */
    suspend fun startWorkout(
        routineId: String,
        routineName: String,
        exerciseCount: Int,
        duration: Int
    ): Boolean {
        return try {
            val workout = ActiveWorkoutEntity(
                routineId = routineId,
                routineName = routineName,
                exerciseCount = exerciseCount,
                duration = duration,
                startedAt = System.currentTimeMillis()
            )
            workoutDao.startWorkout(workout)
            println("✅ Entrenamiento iniciado: $routineName")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Finaliza el entrenamiento actual
     */
    suspend fun finishWorkout(): Boolean {
        return try {
            workoutDao.clearActiveWorkout()
            println("✅ Entrenamiento finalizado")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Obtiene el entrenamiento activo
     */
    suspend fun getActiveWorkout(): ActiveWorkoutEntity? {
        return try {
            workoutDao.getActiveWorkout()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Verifica si hay un entrenamiento activo
     */
    suspend fun hasActiveWorkout(): Boolean {
        return try {
            workoutDao.hasActiveWorkout()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}