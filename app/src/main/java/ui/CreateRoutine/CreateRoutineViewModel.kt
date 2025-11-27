package ui.CreateRoutine

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import data.*
import data.network.AuthPreferencesManager
import data.room.CachedExerciseEntity
import data.room.DraftRoutineEntity
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import ui.AppConfig

class CreateRoutineViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val draftDao = database.draftRoutineDao()
    private val cacheDao = database.cachedExerciseDao()
    private val authDataStore = AuthPreferencesManager(application)
    private val muscleWikiBaseUrl = "https://musclewiki.com"
    private val yourApiBaseUrl = AppConfig.getBaseUrl() + ":9021/api/v1"

    private var currentToken: String? = null

        private val muscleWikiClient = HttpClient {
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

        private val yourApiClient = HttpClient {
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
        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(currentToken ?: "", currentToken ?: "")
                }
            }
        }
    }

        var routineName by mutableStateOf("")
    var routineDescription by mutableStateOf("")
        private set
    var routineDuration by mutableStateOf("")
        private set

        var availableExercises by mutableStateOf<List<Exercise>>(emptyList())
        private set
    var selectedExercises by mutableStateOf<List<RoutineExercise>>(emptyList())
        private set
    var filteredExercises by mutableStateOf<List<Exercise>>(emptyList())
        private set
    var searchQuery by mutableStateOf("")
    var selectedMuscle by mutableStateOf<String?>(null)
    var selectedType by mutableStateOf<String?>(null)

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
    var isAuthenticated by mutableStateOf(false)

    init {
        loadTokenAndData()
    }

    private fun loadTokenAndData() {
        viewModelScope.launch {
            isLoading = true
            try {
                                currentToken = authDataStore.getToken()
                isAuthenticated = currentToken != null

                if (!isAuthenticated) {
                    message = "Debes iniciar sesión para crear rutinas"
                    isLoading = false
                    return@launch
                }

                println("🔐 Token cargado: ${currentToken?.take(10)}...")

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

                                loadExercisesFromMuscleWiki()

            } catch (e: Exception) {
                e.printStackTrace()
                message = "Error al cargar datos: ${e.message}"
                loadDemoExercises()             } finally {
                isLoading = false
            }
        }
    }

    private suspend fun loadExercisesFromMuscleWiki() {
        try {
            println("🔄 Cargando ejercicios desde MuscleWiki API...")
            availableExercises = getAllExercisesFromMuscleWiki()
            filteredExercises = availableExercises

                        val cachedExercises = availableExercises.map {
                CachedExerciseEntity(exercise = it)
            }
            cacheDao.cacheExercises(cachedExercises)

            println("✅ ${availableExercises.size} ejercicios cargados desde MuscleWiki y cacheados")
        } catch (e: Exception) {
            e.printStackTrace()
            message = "Error al cargar ejercicios: ${e.message}"
            loadDemoExercises()         }
    }

    private suspend fun getAllExercisesFromMuscleWiki(): List<Exercise> {
        return try {
            val response: List<MuscleWikiExerciseResponse> = muscleWikiClient.get(
                "$muscleWikiBaseUrl/api-next/exercises/directory?difficulty=1,2,3,4"
            ).body()

            println("📥 Ejercicios recibidos de MuscleWiki: ${response.size}")

                        val exercises = response.map { muscleWikiExercise ->
                Exercise(
                    name = muscleWikiExercise.name ?: "Unknown",
                    type = muscleWikiExercise.category?.name ?: "Unknown",
                    muscle = muscleWikiExercise.muscles?.firstOrNull()?.name ?: "Unknown",
                    equipment = muscleWikiExercise.equipment?.firstOrNull()?.name ?: "None",
                    difficulty = mapDifficulty(muscleWikiExercise.difficulty?.toString()),
                    instructions = muscleWikiExercise.description ?: "No instructions available"
                )
            }

            println("✅ Ejercicios convertidos: ${exercises.size}")
            exercises

        } catch (e: Exception) {
            println("❌ Error al cargar ejercicios desde MuscleWiki: ${e.message}")
                        getDemoExercises()
        }
    }

    private fun mapDifficulty(difficulty: String?): String {
        return when (difficulty) {
            "1" -> "Beginner"
            "2" -> "Intermediate"
            "3" -> "Advanced"
            "4" -> "Expert"
            else -> "Intermediate"         }
    }

    private fun handleAuthError() {
        viewModelScope.launch {
            authDataStore.clearAuthData()
            currentToken = null
            isAuthenticated = false
            message = "Sesión expirada. Por favor inicia sesión nuevamente."
        }
    }

    private fun loadDemoExercises() {
        availableExercises = getDemoExercises()
        filteredExercises = availableExercises
        println("📋 Usando ejercicios demo: ${availableExercises.size}")
    }

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
        if (!isAuthenticated) {
            message = "Debes iniciar sesión para agregar ejercicios"
            return
        }
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

        fun saveRoutine(onSuccess: () -> Unit) {
        if (!isAuthenticated) {
            message = "Debes iniciar sesión para guardar rutinas"
            return
        }
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
                                val exerciseIds = mutableMapOf<String, Long>() 
                for (routineExercise in selectedExercises) {
                    val exerciseName = routineExercise.exercise.name
                    if (!exerciseIds.containsKey(exerciseName)) {
                        val exerciseId = createOrGetExerciseInApi(routineExercise.exercise)
                        exerciseIds[exerciseName] = exerciseId
                        println("✅ Ejercicio '$exerciseName' creado/obtenido con ID: $exerciseId")
                    }
                }

                                val routineId = createWorkoutRoutineInApi()
                println("✅ Rutina creada con ID: $routineId")

                                for (routineExercise in selectedExercises) {
                    val response: Exercise = searchExerciseByName(routineExercise.exercise.name)
                    val exerciseId = response.id
                        ?: throw Exception("No se encontró ID para ejercicio: ${routineExercise.exercise.name}")

                    addExerciseToRoutineInApi(exerciseId, routineId, routineExercise)
                }

                message = "Rutina '$routineName' creada con éxito con ${selectedExercises.size} ejercicios"

                                clearDraft()

                onSuccess()
            } catch (e: Exception) {
                message = "Error al guardar: ${e.message}"
                e.printStackTrace()
                                if (e.message?.contains("401") == true || e.message?.contains("403") == true) {
                    handleAuthError()
                }
            } finally {
                isSaving = false
            }
        }
    }

    private suspend fun searchExerciseByName(name: String): Exercise {
        val exercises: List<Exercise> = yourApiClient.get("$yourApiBaseUrl/exercises") {
            headers {
                append("Authorization", "Bearer $currentToken")
            }
            parameter("name", name)
        }.body()
        return exercises[0]
    }

    private suspend fun createWorkoutRoutineInApi(): Long {
        println("🔄 Creando rutina en API...")
        val username = authDataStore.username.first()
        if (username == null) {
            throw Exception("No se pudo obtener el nombre de usuario")
        }

        println("👤 Usuario obtenido: $username")
        val workoutRoutineRequest = CreateWorkoutRoutineRequest(
            name = routineName,
            description = "testing",
            duration = "1200",
            username = username
        )

        val response: WorkoutRoutine = yourApiClient.post("$yourApiBaseUrl/workout-routines") {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $currentToken")
            }
            setBody(workoutRoutineRequest)
        }.body()

        println("✅ Rutina creada con ID: ${response.id}")
        return response.id ?: throw Exception("No se pudo obtener el ID de la rutina creada")
    }

    private suspend fun createOrGetExerciseInApi(exercise: Exercise): Long {
        println("🔄 Creando/Obteniendo ejercicio: ${exercise.name}")

                val existingExercise = try {
            val exercises: List<Exercise> = yourApiClient.get("$yourApiBaseUrl/exercises") {
                headers {
                    append("Authorization", "Bearer $currentToken")
                }
                parameter("name", exercise.name)
            }.body()
            exercises.firstOrNull { it.name.equals(exercise.name, ignoreCase = true) }
        } catch (e: Exception) {
            null
        }

        return if (existingExercise != null) {
                        println("✅ Ejercicio ya existe con ID: ${existingExercise.id}")
            existingExercise.id ?: throw Exception("El ejercicio existente no tiene ID")
        } else {
                        val request = AddExerciseRequestDTO(
                name = exercise.name,
                type = exercise.type,
                muscle = exercise.muscle,
                equipment = exercise.equipment,
                difficulty = exercise.difficulty,
                instructions = exercise.instructions
            )

            val response: Exercise = yourApiClient.post("$yourApiBaseUrl/exercises") {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $currentToken")
                }
                setBody(request)
            }.body()

            println("✅ Ejercicio creado con ID: ${response.id}")
            response.id ?: throw Exception("No se pudo obtener el ID del ejercicio creado")
        }
    }

    private suspend fun addExerciseToRoutineInApi(
        exerciseId: Long,
        routineId: Long,
        routineExercise: RoutineExercise
    ) {
        println("🔄 Agregando ejercicio $exerciseId a rutina $routineId")

        val request = CreateRoutineExerciseRequest(
            exerciseId = exerciseId,
            workoutRoutineId = routineId,
            sets = routineExercise.sets,
            reps = routineExercise.reps,
            restTime = routineExercise.restTime
        )

        val response = yourApiClient.post("$yourApiBaseUrl/routine-exercises") {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $currentToken")
            }
            setBody(request)
        }

        val statusCode = response.status.value
        println("✅ Ejercicio agregado a rutina. Status: $statusCode")
    }

        fun filterExercises() {
        filteredExercises = availableExercises.filter { exercise ->
            (searchQuery.isEmpty() || exercise.name.contains(searchQuery, ignoreCase = true)) &&
                    (selectedMuscle == null || exercise.muscle.equals(selectedMuscle, ignoreCase = true)) &&
                    (selectedType == null || exercise.type.equals(selectedType, ignoreCase = true))
        }
    }

    fun openExerciseDialog(exercise: Exercise) {
        if (!isAuthenticated) {
            message = "Debes iniciar sesión para agregar ejercicios"
            return
        }
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
        if (!isAuthenticated) {
            message = "Debes iniciar sesión para agregar ejercicios"
            return
        }
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

                        if (hasDraftLoaded) autosaveDraft()
        }
    }

    fun removeExerciseFromRoutine(exercise: RoutineExercise) {
        selectedExercises = selectedExercises - exercise
        if (hasDraftLoaded) autosaveDraft()
    }

    fun clearDraft() {
        viewModelScope.launch {
            try {
                draftDao.clearDraft()
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

        private fun getDemoExercises(): List<Exercise> {
        return listOf(
            Exercise(name = "Push-ups", type = "Bodyweight", muscle = "Chest", equipment = "None", difficulty = "Beginner", instructions = "Classic push-up"),
            Exercise(name = "Squats", type = "Bodyweight", muscle = "Legs", equipment = "None", difficulty = "Beginner", instructions = "Basic squat"),
            Exercise(name = "Plank", type = "Bodyweight", muscle = "Core", equipment = "None", difficulty = "Beginner", instructions = "Hold plank"),
            Exercise(name = "Bench Press", type = "Barbell", muscle = "Chest", equipment = "Barbell", difficulty = "Intermediate", instructions = "Flat bench press"),
            Exercise(name = "Deadlift", type = "Barbell", muscle = "Back", equipment = "Barbell", difficulty = "Advanced", instructions = "Conventional deadlift")
        )
    }

    override fun onCleared() {
        super.onCleared()
        muscleWikiClient.close()
        yourApiClient.close()
    }
}

@kotlinx.serialization.Serializable
data class MuscleWikiExerciseResponse(
    val id: Int? = null,
    val name: String? = null,
    val description: String? = null,
    val category: MuscleWikiCategory? = null,
    val equipment: List<MuscleWikiEquipment>? = null,
    val muscles: List<MuscleWikiMuscle>? = null,
    val muscles_secondary: List<MuscleWikiMuscle>? = null,

    val difficulty: MuscleWikiDifficulty? = null
)

@kotlinx.serialization.Serializable
data class MuscleWikiCategory(
    val id: Int? = null,
    val name: String? = null
)
@kotlinx.serialization.Serializable
data class MuscleWikiDifficulty(
    val id: Int? = null,
    val name: String? = null
)

@kotlinx.serialization.Serializable
data class MuscleWikiEquipment(
    val id: Int? = null,
    val name: String? = null
)

@kotlinx.serialization.Serializable
data class MuscleWikiMuscle(
    val id: Int? = null,
    val name: String? = null
)

@kotlinx.serialization.Serializable
data class CreateRoutineExerciseRequest(
    val exerciseId: Long,
    val workoutRoutineId: Long,
    val sets: Int,
    val reps: Int,
    val restTime: Int? = null
)

@kotlinx.serialization.Serializable
data class CreateWorkoutRoutineRequest(
    val name: String,
    val description: String,
    val duration: String,
    val username: String
)

@kotlinx.serialization.Serializable
data class RoutineExerciseResponse(
    val id: Long? = null,
    val exerciseId: Long,
    val workoutRoutineId: Long,
    val sets: Int,
    val reps: Int,
    val restTime: Int? = null
)

@kotlinx.serialization.Serializable
data class AddExerciseRequestDTO(
    val name: String,
    val type: String,
    val muscle: String,
    val equipment: String,
    val difficulty: String,
    val instructions: String
)