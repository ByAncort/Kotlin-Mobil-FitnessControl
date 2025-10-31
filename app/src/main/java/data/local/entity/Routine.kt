package data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import data.model.RoutineExercise
import kotlinx.serialization.Serializable

@Entity(tableName = "routines")
@Serializable
data class Routine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val duration: Int, // en minutos
    val exercises: List<RoutineExercise>,
    val createdAt: Long = System.currentTimeMillis()
)