package data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import data.room.CachedExerciseDao
import data.room.CachedExerciseEntity
import data.room.DraftRoutineDao
import data.room.DraftRoutineEntity

@Database(
    entities = [DraftRoutineEntity::class, CachedExerciseEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun draftRoutineDao(): DraftRoutineDao
    abstract fun cachedExerciseDao(): CachedExerciseDao

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