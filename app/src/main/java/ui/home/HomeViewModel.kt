package ui.home

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import data.AppDatabase
import data.local.entity.ActiveWorkoutEntity
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val workoutDao = database.activeWorkoutDao()

    private val _uiState = mutableStateOf(HomeUiState())
    val uiState: State<HomeUiState> = _uiState

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Cargar entrenamiento activo (si existe)
                val activeWorkout = workoutDao.getActiveWorkout()

                // Cargar rutinas del usuario (simuladas por ahora)
                val myRoutines = listOf(
                    RoutineUiState(
                        id = "1",
                        name = "Rutina Full Body",
                        exerciseCount = 8,
                        duration = 45,
                        lastCompleted = "Hace 2 días"
                    ),
                    RoutineUiState(
                        id = "2",
                        name = "Pierna y Glúteos",
                        exerciseCount = 6,
                        duration = 35,
                        lastCompleted = "Hace 1 semana"
                    )
                )

                _uiState.value = HomeUiState(
                    myRoutines = myRoutines,
                    todayWorkout = activeWorkout?.let {
                        TodayWorkoutUiState(
                            routineId = it.routineId,
                            routineName = it.routineName,
                            exerciseCount = it.exerciseCount,
                            duration = it.duration,
                            startedAt = it.startedAt
                        )
                    },
                    isLoading = false
                )

                println("📊 Entrenamiento activo: ${if (activeWorkout != null) "Sí" else "No"}")
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = HomeUiState(isLoading = false)
            }
        }
    }

    // Método para iniciar un entrenamiento
    fun startWorkout(
        routineId: String,
        routineName: String,
        exerciseCount: Int,
        duration: Int
    ) {
        viewModelScope.launch {
            try {
                val workout = ActiveWorkoutEntity(
                    routineId = routineId,
                    routineName = routineName,
                    exerciseCount = exerciseCount,
                    duration = duration,
                    startedAt = System.currentTimeMillis()
                )
                workoutDao.startWorkout(workout)
                println("✅ Entrenamiento iniciado: $routineName")
                loadHomeData() // Recargar para mostrar la sección
            } catch (e: Exception) {
                e.printStackTrace()
                println("❌ Error al iniciar entrenamiento: ${e.message}")
            }
        }
    }

    // Método para finalizar el entrenamiento desde Home
    fun finishWorkout() {
        viewModelScope.launch {
            try {
                workoutDao.clearActiveWorkout()
                println("✅ Entrenamiento finalizado")
                loadHomeData() // Recargar para ocultar la sección
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refreshData() {
        loadHomeData()
    }
}

// Estados del UI
data class HomeUiState(
    val myRoutines: List<RoutineUiState> = emptyList(),
    val todayWorkout: TodayWorkoutUiState? = null,
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
    val duration: Int,
    val startedAt: Long = System.currentTimeMillis()
)