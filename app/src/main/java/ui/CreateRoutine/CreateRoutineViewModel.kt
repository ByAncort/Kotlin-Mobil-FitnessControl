package ui.CreateRoutine

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import data.remote.model.ApiExerciseResponse
import data.AppDatabase
import data.remote.model.Exercise
import data.model.RoutineExercise

import data.local.entity.CachedExerciseEntity
import data.local.entity.DraftRoutineEntity
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class CreateRoutineViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val draftDao = database.draftRoutineDao()
    private val cacheDao = database.cachedExerciseDao()

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
        private set
    var routineDuration by mutableStateOf("")
        private set

    // Estados para la selección de ejercicios
    var availableExercises by mutableStateOf<List<Exercise>>(emptyList())
        private set
    var selectedExercises by mutableStateOf<List<RoutineExercise>>(emptyList())
        private set
    var filteredExercises by mutableStateOf<List<Exercise>>(emptyList())
        private set
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
    var hasDraftLoaded by mutableStateOf(false)

    init {
        loadDraftAndExercises()
    }

    private fun loadDraftAndExercises() {
        viewModelScope.launch {
            isLoading = true
            try {
                // Cargar borrador si existe
                val draft = draftDao.getDraftOnce()
                if (draft != null) {
                    routineName = draft.routineName
                    routineDescription = draft.routineDescription
                    routineDuration = draft.routineDuration
                    selectedExercises = draft.selectedExercises

                    if (selectedExercises.isNotEmpty()) {
                        showEmptyState = false
                        showRoutineForm = true
                    }

                    println("✅ Borrador cargado: ${selectedExercises.size} ejercicios")
                }
                hasDraftLoaded = true

                // Intentar cargar ejercicios desde caché primero
                val cachedCount = cacheDao.getCacheCount()
                if (cachedCount > 0) {
                    val cached = cacheDao.getAllCachedExercises()
                    availableExercises = cached.map { it.exercise }
                    filteredExercises = availableExercises
                    println("✅ ${availableExercises.size} ejercicios cargados desde caché")
                } else {
                    // Si no hay caché, cargar desde API
                    loadExercisesFromApi()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                message = "Error al cargar datos: ${e.message}"
                loadExercisesFromApi() // Fallback
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun loadExercisesFromApi() {
        try {
            availableExercises = getAllExercises()
            filteredExercises = availableExercises

            // Cachear ejercicios
            val cachedExercises = availableExercises.map {
                CachedExerciseEntity(exercise = it)
            }
            cacheDao.cacheExercises(cachedExercises)

            println("✅ ${availableExercises.size} ejercicios cargados desde API y cacheados")
        } catch (e: Exception) {
            e.printStackTrace()
            message = "Error al cargar ejercicios: ${e.message}"
        }
    }

    // Auto-guardar cuando cambian los datos
    private fun autosaveDraft() {
        viewModelScope.launch {
            try {
                val draft = DraftRoutineEntity(
                    routineName = routineName,
                    routineDescription = routineDescription,
                    routineDuration = routineDuration,
                    selectedExercises = selectedExercises
                )
                draftDao.saveDraft(draft)
                println("💾 Borrador auto-guardado")
            } catch (e: Exception) {
                println("❌ Error al auto-guardar: ${e.message}")
            }
        }
    }

    fun updateRoutineName(name: String) {
        routineName = name
        if (hasDraftLoaded) autosaveDraft()
    }

    fun updateRoutineDescription(desc: String) {
        routineDescription = desc
        if (hasDraftLoaded) autosaveDraft()
    }

    fun updateRoutineDuration(duration: String) {
        routineDuration = duration
        if (hasDraftLoaded) autosaveDraft()
    }

    fun showAddExerciseScreen() {
        showExerciseSelection = true
        showEmptyState = false
    }

    fun hideExerciseSelection() {
        showExerciseSelection = false
        showEmptyState = selectedExercises.isEmpty()
    }

    fun finishExerciseSelection() {
        showExerciseSelection = false
        showEmptyState = selectedExercises.isEmpty()
        if (selectedExercises.isNotEmpty()) {
            showRoutineForm = true
        }
    }

    private suspend fun getAllExercises(): List<Exercise> {
        return try {
            val response = client.get("https://musclewiki.com/api-next/exercises/directory?difficulty=4%2C3%2C2%2C1")
                .body<List<ApiExerciseResponse>>()

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
            getDemoExercises()
        }
    }

    private fun getDemoExercises(): List<Exercise> {
        return listOf(
            Exercise("Push-ups", "Bodyweight", "Chest", "None", "Beginner", "Classic push-up"),
            Exercise("Squats", "Bodyweight", "Legs", "None", "Beginner", "Basic squat"),
            Exercise("Plank", "Bodyweight", "Core", "None", "Beginner", "Hold plank"),
            Exercise("Lunges", "Bodyweight", "Legs", "None", "Beginner", "Forward lunges"),
            Exercise("Dumbbell Curl", "Dumbbell", "Biceps", "Dumbbell", "Beginner", "Bicep curls"),
            Exercise("Bench Press", "Barbell", "Chest", "Barbell", "Intermediate", "Flat bench"),
            Exercise("Deadlift", "Barbell", "Back", "Barbell", "Intermediate", "Deadlift"),
            Exercise("Pull-ups", "Bodyweight", "Back", "Pull-up bar", "Intermediate", "Pull-ups"),
            Exercise("Shoulder Press", "Dumbbell", "Shoulders", "Dumbbell", "Beginner", "Overhead press"),
            Exercise("Leg Press", "Machine", "Legs", "Machine", "Beginner", "Leg press")
        )
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

            // Auto-guardar
            if (hasDraftLoaded) autosaveDraft()
        }
    }

    fun removeExerciseFromRoutine(exercise: RoutineExercise) {
        selectedExercises = selectedExercises - exercise

        // Auto-guardar
        if (hasDraftLoaded) autosaveDraft()
    }

    fun saveRoutine(onSuccess: () -> Unit) {
        if (routineName.isBlank()) {
            message = "El nombre es obligatorio"
            return
        }
        if (selectedExercises.isEmpty()) {
            message = "Debe agregar al menos un ejercicio"
            return
        }

        viewModelScope.launch {
            isSaving = true
            try {
                // Simular guardado (después implementarás el guardado real)
                delay(1000)

                println("✅ Rutina guardada: $routineName con ${selectedExercises.size} ejercicios")
                message = "Rutina '$routineName' creada con éxito"

                // Limpiar borrador
                clearDraft()

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

    fun clearDraft() {
        viewModelScope.launch {
            try {
                draftDao.clearDraft()

                // Limpiar campos
                routineName = ""
                routineDescription = ""
                routineDuration = ""
                selectedExercises = emptyList()
                showRoutineForm = false
                showEmptyState = true

                println("🗑️ Borrador eliminado")
            } catch (e: Exception) {
                println("❌ Error al limpiar borrador: ${e.message}")
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