package data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import data.local.dao.ActiveWorkoutDao
import data.local.entity.ActiveWorkoutEntity
import data.local.dao.CachedExerciseDao
import data.local.entity.CachedExerciseEntity
import data.local.dao.DraftRoutineDao
import data.local.entity.DraftRoutineEntity

@Database(
    entities = [DraftRoutineEntity::class, CachedExerciseEntity::class, ActiveWorkoutEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun draftRoutineDao(): DraftRoutineDao
    abstract fun cachedExerciseDao(): CachedExerciseDao
    abstract fun activeWorkoutDao(): ActiveWorkoutDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gym_routine_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}