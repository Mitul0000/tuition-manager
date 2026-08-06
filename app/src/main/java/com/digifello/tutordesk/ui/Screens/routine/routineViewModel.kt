package com.digifello.tutordesk.ui.Screens.routine


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digifello.tutordesk.data.model.Batch
import com.digifello.tutordesk.data.repository.BatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Calendar

val WEEKDAYS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

private fun todayKey(): String {
    val cal = Calendar.getInstance()
    val index = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
    return WEEKDAYS[index]
}

data class RoutineUiState(
    val isLoading: Boolean = true,
    val batchesByDay: Map<String, List<Batch>> = emptyMap(),
    val selectedDay: String = todayKey(),
    val errorMessage: String? = null
)

class RoutineViewModel(
    private val batchRepository: BatchRepository = BatchRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutineUiState())
    val uiState: StateFlow<RoutineUiState> = _uiState.asStateFlow()

    init {
        observeBatches()
    }

    private fun observeBatches() {
        viewModelScope.launch {
            batchRepository.getBatches()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Could not load routine."
                    )
                }
                .collect { batches ->
                    val grouped = WEEKDAYS.associateWith { day ->
                        batches
                            .filter { it.days.contains(day) }
                            .sortedBy { it.time }
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        batchesByDay = grouped,
                        errorMessage = null
                    )
                }
        }
    }

    fun selectDay(day: String) {
        if (WEEKDAYS.contains(day)) {
            _uiState.value = _uiState.value.copy(selectedDay = day)
        }
    }
}