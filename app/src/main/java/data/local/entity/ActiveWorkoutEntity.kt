package data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_workout")
data class ActiveWorkoutEntity(
    @PrimaryKey
    val id: Int = 1,
    val routineId: String,
    val routineName: String,
    val exerciseCount: Int,
    val duration: Int,
    val startedAt: Long = System.currentTimeMillis()
)