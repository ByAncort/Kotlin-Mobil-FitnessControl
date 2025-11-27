package data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
//
//    @Query("SELECT * FROM routines ORDER BY createdAt DESC")
//    fun getAllRoutines(): Flow<List<Routine>>
//
//    @Query("SELECT * FROM routines WHERE id = :routineId")
//    suspend fun getRoutineById(routineId: Long): Routine?
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertRoutine(routine: Routine): Long
//
//    @Update
//    suspend fun updateRoutine(routine: Routine)
//
//    @Delete
//    suspend fun deleteRoutine(routine: Routine)
//
//    @Query("DELETE FROM routines WHERE id = :routineId")
//    suspend fun deleteRoutineById(routineId: Long)
//
//    @Query("SELECT COUNT(*) FROM routines")
//    suspend fun getRoutineCount(): Int
}
