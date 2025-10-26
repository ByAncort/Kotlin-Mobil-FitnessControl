package data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson

import data.Exercise
import data.RoutineExercise

// Entidad para guardar el borrador de la rutina
@Entity(tableName = "draft_routine")
@TypeConverters(DraftConverters::class)
data class DraftRoutineEntity(
    @PrimaryKey
    val id: Int = 1, // Solo habrá un borrador activo
    val routineName: String = "",
    val routineDescription: String = "",
    val routineDuration: String = "",
    val selectedExercises: List<RoutineExercise> = emptyList(),
    val lastModified: Long = System.currentTimeMillis()
)

// Convertidores para tipos complejos
class DraftConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromRoutineExerciseList(value: List<RoutineExercise>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toRoutineExerciseList(value: String): List<RoutineExercise> {
        val listType = object : TypeToken<List<RoutineExercise>>() {}.type
        return gson.fromJson(value, listType)
    }
}

// Entidad para ejercicios cacheados
@Entity(tableName = "cached_exercises")
@TypeConverters(ExerciseConverters::class)
data class CachedExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val exercise: Exercise,
    val cachedAt: Long = System.currentTimeMillis()
)

class ExerciseConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromExercise(value: Exercise): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toExercise(value: String): Exercise {
        return gson.fromJson(value, Exercise::class.java)
    }
}