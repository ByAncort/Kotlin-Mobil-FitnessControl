package ui.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _uiState = mutableStateOf(HomeUiState())
    val uiState: State<HomeUiState> = _uiState

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Simular carga de datos
            kotlinx.coroutines.delay(1000)

            _uiState.value = HomeUiState(
                myRoutines = listOf(
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
                ),
                todayWorkout = TodayWorkoutUiState(
                    routineId = "1",
                    routineName = "Rutina Full Body",
                    exerciseCount = 8,
                    duration = 45
                ),
                isLoading = false
            )
        }
    }

    fun refreshData() {
        loadHomeData()
    }
}

// Estados del UI (igual que arriba)
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
    val duration: Int
)