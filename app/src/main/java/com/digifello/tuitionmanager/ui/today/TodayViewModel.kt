package com.digifello.tuitionmanager.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digifello.tuitionmanager.data.model.Batch
import com.digifello.tuitionmanager.data.repository.BatchRepository
import com.digifello.tuitionmanager.ui.common.UiState
import com.digifello.tuitionmanager.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TodayViewModel(
    private val batchRepository: BatchRepository = BatchRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Batch>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Batch>>> = _uiState.asStateFlow()

    val todayName: String = DateUtils.currentDayName()
    val todayDateFormatted: String = DateUtils.formattedTodayDate()

    init {
        loadTodaysBatches()
    }

    private fun loadTodaysBatches() {
        viewModelScope.launch {
            batchRepository.getBatchesFlow().collect { batches ->
                // Filter to only batches scheduled for today's weekday,
                // then sort by time so the list reads top-to-bottom chronologically
                val todaysBatches = batches
                    .filter { it.days.contains(todayName) }
                    .sortedBy { it.time }
                _uiState.value = UiState.Success(todaysBatches)
            }
        }
    }
}