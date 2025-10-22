package data

data class Routine(
    val id: String? = null,
    val name: String,
    val description: String,
    val duration: Int,
    val exercises: List<RoutineExercise> = emptyList(),
    val createdAt: String? = null
)