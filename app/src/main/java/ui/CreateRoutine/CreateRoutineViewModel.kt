package ui.CreateRoutine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.ApiExerciseResponse
import data.Exercise
import data.RoutineExercise
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class CreateRoutineViewModel : ViewModel() {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = false
                isLenient = true
            })
        }
    }

    // Campos de la rutina
    var routineName by mutableStateOf("")
    var routineDescription by mutableStateOf("")
    var routineDuration by mutableStateOf("")

    // Estados para la selección de ejercicios
    var availableExercises by mutableStateOf<List<Exercise>>(emptyList())
    var selectedExercises by mutableStateOf<List<RoutineExercise>>(emptyList())
    var filteredExercises by mutableStateOf<List<Exercise>>(emptyList())
    var searchQuery by mutableStateOf("")
    var selectedMuscle by mutableStateOf<String?>(null)
    var selectedType by mutableStateOf<String?>(null)

    // Estados de UI
    var isLoading by mutableStateOf(false)
    var isSaving by mutableStateOf(false)
    var message by mutableStateOf<String?>(null)
    var showExerciseDialog by mutableStateOf(false)
    var currentExercise by mutableStateOf<Exercise?>(null)
    var sets by mutableStateOf("3")
    var reps by mutableStateOf("10")
    var restTime by mutableStateOf("60")
    var showExerciseSelection by mutableStateOf(false)
    var showEmptyState by mutableStateOf(true)
    var showRoutineForm by mutableStateOf(false)

    fun showAddExerciseScreen() {
        showExerciseSelection = true
        showEmptyState = false
    }

    fun hideExerciseSelection() {
        showExerciseSelection = false
        showEmptyState = true
    }

    fun finishExerciseSelection() {
        showExerciseSelection = false
        showEmptyState = selectedExercises.isEmpty()
        if (selectedExercises.isNotEmpty()) {
            showRoutineForm = true
        }
    }

    init {
        loadExercises()
    }

    private suspend fun getAllExercises(): List<Exercise> {
        return try {
            // Llamada a la API
            val response = client.get("https://musclewiki.com/api-next/exercises/directory?difficulty=1")
                .body<List<ApiExerciseResponse>>()

            // Convertir al formato simple
            response.map { apiExercise ->
                Exercise(
                    name = apiExercise.name,
                    type = apiExercise.category.name,
                    muscle = apiExercise.muscles.firstOrNull()?.name ?: "Unknown",
                    equipment = apiExercise.category.name,
                    difficulty = apiExercise.difficulty.name,
                    instructions = ""
                )
            }
        } catch (e: Exception) {
            println("❌ Error al cargar ejercicios: ${e.message}")
            e.printStackTrace()
            // Retornar ejercicios de ejemplo en caso de error
            getDemoExercises()
        }
    }

    private fun getDemoExercises(): List<Exercise> {
        return listOf(
            Exercise(
                name = "Push-ups",
                type = "Bodyweight",
                muscle = "Chest",
                equipment = "None",
                difficulty = "Beginner",
                instructions = "Classic push-up exercise"
            ),
            Exercise(
                name = "Squats",
                type = "Bodyweight",
                muscle = "Legs",
                equipment = "None",
                difficulty = "Beginner",
                instructions = "Basic squat movement"
            ),
            Exercise(
                name = "Plank",
                type = "Bodyweight",
                muscle = "Core",
                equipment = "None",
                difficulty = "Beginner",
                instructions = "Hold plank position"
            ),
            Exercise(
                name = "Lunges",
                type = "Bodyweight",
                muscle = "Legs",
                equipment = "None",
                difficulty = "Beginner",
                instructions = "Forward lunges"
            ),
            Exercise(
                name = "Dumbbell Curl",
                type = "Dumbbell",
                muscle = "Biceps",
                equipment = "Dumbbell",
                difficulty = "Beginner",
                instructions = "Bicep curls with dumbbells"
            ),
            Exercise(
                name = "Bench Press",
                type = "Barbell",
                muscle = "Chest",
                equipment = "Barbell",
                difficulty = "Intermediate",
                instructions = "Flat bench press"
            ),
            Exercise(
                name = "Deadlift",
                type = "Barbell",
                muscle = "Back",
                equipment = "Barbell",
                difficulty = "Intermediate",
                instructions = "Conventional deadlift"
            ),
            Exercise(
                name = "Pull-ups",
                type = "Bodyweight",
                muscle = "Back",
                equipment = "Pull-up bar",
                difficulty = "Intermediate",
                instructions = "Standard pull-ups"
            ),
            Exercise(
                name = "Shoulder Press",
                type = "Dumbbell",
                muscle = "Shoulders",
                equipment = "Dumbbell",
                difficulty = "Beginner",
                instructions = "Overhead shoulder press"
            ),
            Exercise(
                name = "Leg Press",
                type = "Machine",
                muscle = "Legs",
                equipment = "Machine",
                difficulty = "Beginner",
                instructions = "Leg press machine"
            )
        )
    }

    private fun loadExercises() {
        viewModelScope.launch {
            isLoading = true
            try {
                availableExercises = getAllExercises()
                filteredExercises = availableExercises
                println("✅ ${availableExercises.size} ejercicios cargados")
            } catch (e: Exception) {
                e.printStackTrace()
                message = "Error al cargar ejercicios: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun filterExercises() {
        filteredExercises = availableExercises.filter { exercise ->
            (searchQuery.isEmpty() || exercise.name.contains(searchQuery, ignoreCase = true)) &&
                    (selectedMuscle == null || exercise.muscle.equals(selectedMuscle, ignoreCase = true)) &&
                    (selectedType == null || exercise.type.equals(selectedType, ignoreCase = true))
        }
    }

    fun openExerciseDialog(exercise: Exercise) {
        currentExercise = exercise
        sets = "3"
        reps = "10"
        restTime = "60"
        showExerciseDialog = true
    }

    fun closeExerciseDialog() {
        showExerciseDialog = false
        currentExercise = null
    }

    fun addExerciseToRoutine() {
        currentExercise?.let { exercise ->
            val routineExercise = RoutineExercise(
                exercise = exercise,
                sets = sets.toIntOrNull() ?: 3,
                reps = reps.toIntOrNull() ?: 10,
                restTime = restTime.toIntOrNull() ?: 60
            )
            selectedExercises = selectedExercises + routineExercise
            closeExerciseDialog()
            message = "Ejercicio ${exercise.name} agregado"
        }
    }

    fun removeExerciseFromRoutine(exercise: RoutineExercise) {
        selectedExercises = selectedExercises - exercise
    }

    fun saveRoutine(onSuccess: () -> Unit) {
        if (routineName.isBlank()) {
            message = "El nombre es obligatorio"
            return
        }
//        if (routineDuration.isBlank() || routineDuration.toIntOrNull() == null) {
//            message = "La duración debe ser un número válido"
//            return
//        }
        if (selectedExercises.isEmpty()) {
            message = "Debe agregar al menos un ejercicio"
            return
        }

        viewModelScope.launch {
            isSaving = true
            try {
                // Simular guardado (después agregaremos Room)
                delay(1000)

                println("✅ Rutina guardada: $routineName con ${selectedExercises.size} ejercicios")
                message = "Rutina '$routineName' creada con éxito"

                // Limpiar campos
                routineName = ""
                routineDescription = ""
                routineDuration = ""
                selectedExercises = emptyList()
                showRoutineForm = false
                showEmptyState = true

                delay(500)
                onSuccess()
            } catch (e: Exception) {
                message = "Error al guardar: ${e.message}"
                e.printStackTrace()
            } finally {
                isSaving = false
            }
        }
    }

    fun getUniqueMuscles(): List<String> {
        return availableExercises.map { it.muscle }.distinct().sorted()
    }

    fun getUniqueTypes(): List<String> {
        return availableExercises.map { it.type }.distinct().sorted()
    }

    fun messageConsumed() {
        message = null
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}
