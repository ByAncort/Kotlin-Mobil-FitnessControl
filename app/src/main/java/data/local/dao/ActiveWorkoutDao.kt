package data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import data.local.entity.ActiveWorkoutEntity

@Dao
interface ActiveWorkoutDao {

    @Query("SELECT * FROM active_workout WHERE id = 1")
    suspend fun getActiveWorkout(): ActiveWorkoutEntity?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun startWorkout(workout: ActiveWorkoutEntity)

    @Query("DELETE FROM active_workout")
    suspend fun clearActiveWorkout()

    @Query("SELECT COUNT(*) > 0 FROM active_workout")
    suspend fun hasActiveWorkout(): Boolean
}