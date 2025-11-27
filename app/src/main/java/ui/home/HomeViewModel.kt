package ui.home

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import data.AppDatabase
import data.network.AuthPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ui.AppConfig
import java.net.HttpURLConnection
import java.net.URL

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    internal val draftDao = database.draftRoutineDao()
    internal val authDataStore = AuthPreferencesManager(application)

    private val _uiState = mutableStateOf(HomeUiState())
    val uiState: State<HomeUiState> = _uiState

    private val baseUrl = AppConfig.getBaseUrl()+":9021"
    private var currentToken: String? = null

    init {
        loadTokenAndData()
        observeDraftRoutine()
    }

    private fun loadTokenAndData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Cargar token de autenticación
                currentToken = authDataStore.getToken()
                val username = authDataStore.username.first()

                if (currentToken != null && username != null) {
                    // Cargar datos reales desde la API
                    loadRealHomeData(username)
                } else {
                    // Si no hay usuario autenticado, cargar datos vacíos
                    _uiState.value = HomeUiState(
                        myRoutines = emptyList(),
                        todayWorkout = null,
                        draftRoutine = _uiState.value.draftRoutine,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // En caso de error, cargar estado vacío
                _uiState.value = HomeUiState(
                    myRoutines = emptyList(),
                    todayWorkout = null,
                    draftRoutine = _uiState.value.draftRoutine,
                    isLoading = false
                )
            }
        }
    }

    private suspend fun loadRealHomeData(username: String) {
        try {
            // Cargar rutinas del usuario desde la API
            val routines = getUserRoutines(username)

            _uiState.value = HomeUiState(
                myRoutines = routines.take(3), // Solo las primeras 3 rutinas
                todayWorkout = getTodayWorkout(routines),
                draftRoutine = _uiState.value.draftRoutine,
                isLoading = false
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // En caso de error, mantener el estado actual pero sin loading
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private suspend fun getUserRoutines(username: String): List<RoutineUiState> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "$baseUrl/api/v1/workout-routines/user/$username"

                val connection = URL(url).openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "GET"
                    setRequestProperty("accept", "*/*")
                    setRequestProperty("Authorization", "Bearer $currentToken")
                    connectTimeout = 15000
                    readTimeout = 15000
                }

                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }

                    val json = Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }

                    val apiRoutines = json.decodeFromString<List<WorkoutRoutineResponse>>(responseText)

                    // Convertir a RoutineUiState
                    apiRoutines.map { apiRoutine ->
                        RoutineUiState(
                            id = apiRoutine.id.toString(),
                            name = apiRoutine.name,
                            exerciseCount = apiRoutine.exercises?.size ?: 0,
                            duration = apiRoutine.duration?.toIntOrNull() ?: 30,
                            lastCompleted = "No completada" // Puedes ajustar esto según tu lógica
                        )
                    }
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    private fun getTodayWorkout(routines: List<RoutineUiState>): TodayWorkoutUiState? {
        // Lógica para determinar el entrenamiento de hoy
        // Por ahora, devolvemos la primera rutina como ejemplo
        return routines.firstOrNull()?.let { routine ->
            TodayWorkoutUiState(
                routineId = routine.id,
                routineName = routine.name,
                exerciseCount = routine.exerciseCount,
                duration = routine.duration
            )
        }
    }

    private fun observeDraftRoutine() {
        viewModelScope.launch {
            // Observar cambios en el borrador
            draftDao.getDraft().collect { draft ->
                _uiState.value = _uiState.value.copy(
                    draftRoutine = draft?.let {
                        DraftRoutineUiState(
                            name = draft.routineName.ifEmpty { "Rutina sin título" },
                            exerciseCount = draft.selectedExercises.size,
                            lastModified = "Guardado recientemente"
                        )
                    }
                )
            }
        }
    }

    fun refreshData() {
        loadTokenAndData()
    }

    fun clearDraftRoutine() {
        viewModelScope.launch {
            draftDao.clearDraft()
        }
    }
}

// Modelos para la respuesta de la API
@Serializable
data class WorkoutRoutineResponse(
    val id: Long,
    val name: String,
    val description: String? = null,
    val duration: String? = null,
    val username: String,
    val exercises: List<RoutineExerciseResponse>? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class RoutineExerciseResponse(
    val id: Long? = null,
    val sets: Int,
    val reps: Int,
    @SerialName("rest_time")
    val restTime: Int? = null,
    val exercise: ExerciseResponse? = null
)

@Serializable
data class ExerciseResponse(
    val id: Long,
    val name: String,
    val type: String? = null,
    val muscle: String? = null,
    val equipment: String? = null,
    val difficulty: String? = null,
    val instructions: String? = null
)

// Estados del UI (se mantienen igual)
data class HomeUiState(
    val myRoutines: List<RoutineUiState> = emptyList(),
    val todayWorkout: TodayWorkoutUiState? = null,
    val draftRoutine: DraftRoutineUiState? = null,
    val isLoading: Boolean = false
)

data class RoutineUiState(
    val id: String,
    val name: String,
    val exerciseCount: Int,
    val duration: Int,
    val lastCompleted: String? = null
)

data class TodayWorkoutUiState(
    val routineId: String,
    val routineName: String,
    val exerciseCount: Int,
    val duration: Int
)

data class DraftRoutineUiState(
    val name: String,
    val exerciseCount: Int,
    val lastModified: String
)