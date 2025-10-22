package ui.CreateRoutine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.Exercise
import data.RoutineExercise
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class CreateRoutineViewModel : ViewModel() {

    // Configuración del cliente HTTP con soporte JSON
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true  // Ignora campos desconocidos de la API
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
    var message by mutableStateOf<String?>(null)
    var showExerciseDialog by mutableStateOf(false)
    var currentExercise by mutableStateOf<Exercise?>(null)
    var sets by mutableStateOf("3")
    var reps by mutableStateOf("10")
    var restTime by mutableStateOf("60")
    var showExerciseSelection by mutableStateOf(false)
    var showEmptyState by mutableStateOf(true)
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
    }
    init {
        loadExercises()
    }

    companion object {
        private const val BASE_URL = "https://api.api-ninjas.com/v1"
        private const val API_KEY = "vXO3ReejVqbz3uprfPrG2w==2eIo65wejTCi1UuD"
    }

    // Carga de ejercicios desde la API
    private suspend fun getAllExercises(): List<Exercise> {
        return try {
            client.get("$BASE_URL/exercises") {
                header("X-Api-Key", API_KEY)
                header(HttpHeaders.Accept, "application/json")
            }.body<List<Exercise>>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun loadExercises() {
        viewModelScope.launch {
            isLoading = true
            try {
                // Obtener datos reales de la API
                availableExercises = getAllExercises()
                filteredExercises = availableExercises
                delay(1000) // Simular carga visual
            } catch (e: Exception) {
                e.printStackTrace()
                message = "Error al cargar ejercicios"
            } finally {
                isLoading = false
            }
        }
    }

    // Filtrado de ejercicios según búsqueda y filtros
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

    fun saveRoutine(): Boolean {
        if (routineName.isBlank()) {
            message = "el nombre es obligatorio"
            return false
        }
        if (routineDuration.isBlank() || routineDuration.toIntOrNull() == null) {
            message = "la duracion debe ser un numero valido"
            return false
        }
        if (selectedExercises.isEmpty()) {
            message = "debe agregar al menos un ejercicio"
            return false
        }

        message = "rutina '$routineName' creada con exito"

        // Limpiar campos
        routineName = ""
        routineDescription = ""
        routineDuration = ""
        selectedExercises = emptyList()

        return true
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
}
