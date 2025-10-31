package data.local.dao

import androidx.room.*
import data.local.entity.CachedExerciseEntity
import data.local.entity.DraftRoutineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DraftRoutineDao {

    @Query("SELECT * FROM draft_routine WHERE id = 1")
    fun getDraft(): Flow<DraftRoutineEntity?>

    @Query("SELECT * FROM draft_routine WHERE id = 1")
    suspend fun getDraftOnce(): DraftRoutineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDraft(draft: DraftRoutineEntity)

    @Query("DELETE FROM draft_routine WHERE id = 1")
    suspend fun clearDraft()

    @Query("SELECT COUNT(*) > 0 FROM draft_routine WHERE id = 1")
    suspend fun hasDraft(): Boolean
}

@Dao
interface CachedExerciseDao {

    @Query("SELECT * FROM cached_exercises")
    suspend fun getAllCachedExercises(): List<CachedExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheExercises(exercises: List<CachedExerciseEntity>)

    @Query("DELETE FROM cached_exercises")
    suspend fun clearCache()

    @Query("SELECT COUNT(*) FROM cached_exercises")
    suspend fun getCacheCount(): Int
}