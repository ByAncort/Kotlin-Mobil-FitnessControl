package data

data class RoutineExercise(
    val exercise: Exercise,
    val sets: Int,
    val reps: Int,
    val restTime: Int // en segundos
)