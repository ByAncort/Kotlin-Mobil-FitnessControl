package data

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromRoutineExerciseList(value: List<RoutineExercise>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toRoutineExerciseList(value: String): List<RoutineExercise> {
        return json.decodeFromString(value)
    }
}