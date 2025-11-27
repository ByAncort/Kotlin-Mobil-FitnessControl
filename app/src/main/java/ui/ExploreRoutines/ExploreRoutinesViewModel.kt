package ui.ExploreRoutines

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import data.Exercise
import data.RoutineExercise
import data.WorkoutRoutine
import data.network.AuthPreferencesManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import ui.AppConfig

class ExploreRoutinesViewModel(application: Application) : AndroidViewModel(application) {

    private val authDataStore = AuthPreferencesManager(application)
//    private val baseUrl = "http://10.15.216.159:9021/api/v1"
    private val baseUrl = AppConfig.getBaseUrl()+":9021/api/v1"
    private var currentToken: String? = null

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = false
                isLenient = true
            })
        }
        install(Logging) {
            level = LogLevel.ALL
        }
    }

    // Estados - usando mutableStateOf para Compose
    var routines by mutableStateOf<List<WorkoutRoutine>>(emptyList())
        private set

    var filteredRoutines by mutableStateOf<List<WorkoutRoutine>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var searchQuery by mutableStateOf("")
        private set

    var selectedDifficulty by mutableStateOf<String?>(null)
        private set

    var selectedDuration by mutableStateOf<String?>(null)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var selectedRoutine by mutableStateOf<WorkoutRoutine?>(null)
        private set

    var showRoutineDetail by mutableStateOf(false)
        private set

    // Filtros disponibles
    val difficulties = listOf("Beginner", "Intermediate", "Advanced", "Expert")
    val durations = listOf("15 min", "30 min", "45 min", "60 min", "90 min", "120 min")

    init {
        loadTokenAndRoutines()
    }

    private fun loadTokenAndRoutines() {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                currentToken = authDataStore.getToken()
                if (currentToken != null) {
                    loadRoutinesFromApi()
                } else {
                    error = "Debes iniciar sesión para ver las rutinas"
                    // Cargar datos demo si no hay autenticación
                    loadDemoRoutines()
                }
            } catch (e: Exception) {
                error = "Error al cargar rutinas: ${e.message}"
                e.printStackTrace()
                // Fallback a datos demo
                loadDemoRoutines()
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun loadRoutinesFromApi() {
        try {
            println("🔄 Cargando rutinas desde API...")
            routines = getAllRoutinesFromApi()
            filteredRoutines = routines
            println("✅ ${routines.size} rutinas cargadas desde API")
        } catch (e: Exception) {
            error = "Error al cargar rutinas: ${e.message}"
            e.printStackTrace()
            // Fallback a datos demo
            loadDemoRoutines()
        }
    }

    private suspend fun getAllRoutinesFromApi(): List<WorkoutRoutine> {
        return try {
            val response: List<RoutineExerciseResponse> = client.get("$baseUrl/routine-exercises") {
                headers {
                    append("Authorization", "Bearer $currentToken")
                }
            }.body()

            println("📥 Datos recibidos: ${response.size} ejercicios de rutina")

            // Agrupar los ejercicios por rutina
            val routinesMap = mutableMapOf<Long, WorkoutRoutine>()

            response.forEach { routineExercise ->
                val routineId = routineExercise.workoutRoutine.id
                val workoutRoutine = routinesMap.getOrPut(routineId) {
                    WorkoutRoutine(
                        id = routineExercise.workoutRoutine.id,
                        name = routineExercise.workoutRoutine.name,
                        description = routineExercise.workoutRoutine.description,
                        duration = routineExercise.workoutRoutine.duration,
                        exercises = mutableListOf()
                    )
                }

                // Agregar el ejercicio a la rutina
                (workoutRoutine.exercises as MutableList).add(
                    RoutineExercise(
                        exercise = Exercise(
                            id = routineExercise.exercise.id,
                            name = routineExercise.exercise.name,
                            type = routineExercise.exercise.type,
                            muscle = routineExercise.exercise.muscle,
                            equipment = routineExercise.exercise.equipment,
                            difficulty = routineExercise.exercise.difficulty,
                            instructions = routineExercise.exercise.instructions
                        ),
                        sets = routineExercise.sets,
                        reps = routineExercise.reps,
                        restTime = routineExercise.restTime
                    )
                )
            }

            val routines = routinesMap.values.toList()
            println("✅ Rutinas procesadas: ${routines.size} rutinas únicas")
            routines

        } catch (e: Exception) {
            println("❌ Error al cargar rutinas desde API: ${e.message}")
            if (e.message?.contains("401") == true || e.message?.contains("403") == true) {
                handleAuthError()
            }
            throw e
        }
    }

    private fun handleAuthError() {
        viewModelScope.launch {
            authDataStore.clearAuthData()
            currentToken = null
            error = "Sesión expirada. Por favor inicia sesión nuevamente."
        }
    }

    private fun loadDemoRoutines() {
        routines = getDemoRoutines()
        filteredRoutines = routines
        println("📋 Usando rutinas demo: ${routines.size}")
    }

    // Acciones públicas
    fun refreshRoutines() {
        viewModelScope.launch {
            isRefreshing = true
            error = null
            try {
                loadRoutinesFromApi()
            } catch (e: Exception) {
                error = "Error al actualizar: ${e.message}"
            } finally {
                isRefreshing = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
        filterRoutines()
    }

    fun updateSelectedDifficulty(difficulty: String?) {
        selectedDifficulty = difficulty
        filterRoutines()
    }

    fun updateSelectedDuration(duration: String?) {
        selectedDuration = duration
        filterRoutines()
    }

    private fun filterRoutines() {
        filteredRoutines = routines.filter { routine ->
            val matchesSearch = searchQuery.isEmpty() ||
                    routine.name.contains(searchQuery, ignoreCase = true) ||
                    routine.description?.contains(searchQuery, ignoreCase = true) == true

//            val matchesDifficulty = selectedDifficulty == null ||
//                    routine.difficulty.equals(selectedDifficulty, ignoreCase = true)
//
//            val matchesDuration = selectedDuration == null ||
//                    routine.duration?.contains(selectedDuration, ignoreCase = true) == true

            matchesSearch
        }
        println("🔍 Filtradas ${filteredRoutines.size} rutinas de ${routines.size} totales")
    }

    fun clearFilters() {
        searchQuery = ""
        selectedDifficulty = null
        selectedDuration = null
        filteredRoutines = routines
        println("🧹 Filtros limpiados")
    }

    fun showRoutineDetails(routine: WorkoutRoutine) {
        selectedRoutine = routine
        showRoutineDetail = true
        println("📖 Mostrando detalles de rutina: ${routine.name}")
    }

    fun hideRoutineDetails() {
        showRoutineDetail = false
        selectedRoutine = null
        println("❌ Cerrando detalles de rutina")
    }

    fun startRoutine(routine: WorkoutRoutine) {
        viewModelScope.launch {
            try {
                println("🏁 Iniciando rutina: ${routine.name}")
                // TODO: Implementar lógica de inicio de rutina
                // Por ejemplo: navegar a pantalla de ejecución, guardar rutina actual, etc.

                // Simulación de inicio exitoso
                error = "Rutina '${routine.name}' iniciada correctamente"

            } catch (e: Exception) {
                error = "Error al iniciar rutina: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    fun saveRoutine(routine: WorkoutRoutine) {
        viewModelScope.launch {
            try {
                println("💾 Guardando rutina: ${routine.name}")
                // TODO: Implementar lógica de guardado/favoritos
                // Por ejemplo: llamar a API para marcar como favorita

                // Simulación de guardado exitoso
                error = "Rutina '${routine.name}' guardada en favoritos"

            } catch (e: Exception) {
                error = "Error al guardar rutina: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    fun clearError() {
        error = null
    }

    // Datos demo mejorados
    private fun getDemoRoutines(): List<WorkoutRoutine> {
        return listOf(
            WorkoutRoutine(
                id = 1,
                name = "Rutina Full Body para Principiantes",
                description = "Rutina completa para todo el cuerpo, perfecta para quienes comienzan",
                duration = "45 min",
                exercises = listOf(
                    RoutineExercise(
                        exercise = Exercise(
                            name = "Push-ups",
                            type = "Bodyweight",
                            muscle = "Chest",
                            equipment = "None",
                            difficulty = "Beginner",
                            instructions = "Classic push-up"
                        ),
                        sets = 3,
                        reps = 12,
                        restTime = 60
                    ),
                    RoutineExercise(
                        exercise = Exercise(
                            name = "Bodyweight Squats",
                            type = "Bodyweight",
                            muscle = "Legs",
                            equipment = "None",
                            difficulty = "Beginner",
                            instructions = "Basic bodyweight squat"
                        ),
                        sets = 3,
                        reps = 15,
                        restTime = 60
                    ),
                    RoutineExercise(
                        exercise = Exercise(
                            name = "Plank",
                            type = "Bodyweight",
                            muscle = "Core",
                            equipment = "None",
                            difficulty = "Beginner",
                            instructions = "Hold plank position"
                        ),
                        sets = 3,
                        reps = 30, // segundos
                        restTime = 45
                    )
                )
            ),
            WorkoutRoutine(
                id = 2,
                name = "Upper Body Strength",
                description = "Desarrolla fuerza en torso, hombros y brazos",
                duration = "60 min",
                exercises = listOf(
                    RoutineExercise(
                        exercise = Exercise(
                            name = "Bench Press",
                            type = "Barbell",
                            muscle = "Chest",
                            equipment = "Barbell",
                            difficulty = "Intermediate",
                            instructions = "Flat bench press with barbell"
                        ),
                        sets = 4,
                        reps = 8,
                        restTime = 90
                    ),
                    RoutineExercise(
                        exercise = Exercise(
                            name = "Pull-ups",
                            type = "Bodyweight",
                            muscle = "Back",
                            equipment = "Pull-up bar",
                            difficulty = "Intermediate",
                            instructions = "Pull-ups with proper form"
                        ),
                        sets = 4,
                        reps = 6,
                        restTime = 90
                    ),
                    RoutineExercise(
                        exercise = Exercise(
                            name = "Shoulder Press",
                            type = "Dumbbell",
                            muscle = "Shoulders",
                            equipment = "Dumbbells",
                            difficulty = "Intermediate",
                            instructions = "Dumbbell shoulder press"
                        ),
                        sets = 3,
                        reps = 10,
                        restTime = 75
                    )
                )
            ),
            WorkoutRoutine(
                id = 3,
                name = "Lower Body Power",
                description = "Enfocada en desarrollar potencia en piernas y glúteos",
                duration = "55 min",
                exercises = listOf(
                    RoutineExercise(
                        exercise = Exercise(
                            name = "Barbell Squats",
                            type = "Barbell",
                            muscle = "Legs",
                            equipment = "Barbell",
                            difficulty = "Advanced",
                            instructions = "Barbell back squats"
                        ),
                        sets = 5,
                        reps = 5,
                        restTime = 120
                    ),
                    RoutineExercise(
                        exercise = Exercise(
                            name = "Romanian Deadlifts",
                            type = "Barbell",
                            muscle = "Hamstrings",
                            equipment = "Barbell",
                            difficulty = "Advanced",
                            instructions = "RDL for hamstring development"
                        ),
                        sets = 4,
                        reps = 8,
                        restTime = 90
                    ),
                    RoutineExercise(
                        exercise = Exercise(
                            name = "Lunges",
                            type = "Dumbbell",
                            muscle = "Legs",
                            equipment = "Dumbbells",
                            difficulty = "Intermediate",
                            instructions = "Walking lunges with dumbbells"
                        ),
                        sets = 3,
                        reps = 12,
                        restTime = 60
                    )
                )
            ),
            WorkoutRoutine(
                id = 4,
                name = "Core & Stability",
                description = "Mejora tu core y estabilidad para un mejor rendimiento",
                duration = "30 min",

                exercises = listOf(
                    RoutineExercise(
                        exercise = Exercise(
                            name = "Russian Twists",
                            type = "Bodyweight",
                            muscle = "Core",
                            equipment = "None",
                            difficulty = "Beginner",
                            instructions = "Seated Russian twists"
                        ),
                        sets = 3,
                        reps = 20,
                        restTime = 45
                    ),
                    RoutineExercise(
                        exercise = Exercise(
                            name = "Leg Raises",
                            type = "Bodyweight",
                            muscle = "Core",
                            equipment = "None",
                            difficulty = "Beginner",
                            instructions = "Lying leg raises"
                        ),
                        sets = 3,
                        reps = 15,
                        restTime = 45
                    )
                )
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}
// Agrega estos modelos en tu archivo de datos o en el mismo ViewModel

@kotlinx.serialization.Serializable
data class RoutineExerciseResponse(
    val id: Long,
    val exercise: ExerciseResponse,
    val workoutRoutine: WorkoutRoutineResponse,
    val sets: Int,
    val reps: Int,
    val restTime: Int,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@kotlinx.serialization.Serializable
data class ExerciseResponse(
    val id: Long,
    val name: String,
    val type: String,
    val muscle: String,
    val equipment: String,
    val difficulty: String,
    val instructions: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@kotlinx.serialization.Serializable
data class WorkoutRoutineResponse(
    val id: Long,
    val name: String,
    val description: String,
    val duration: String,
    val createdAt: String,
    val updatedAt: String
)