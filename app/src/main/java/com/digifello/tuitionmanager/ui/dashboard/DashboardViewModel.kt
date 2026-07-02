package com.digifello.tuitionmanager.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digifello.tuitionmanager.data.model.Batch
import com.digifello.tuitionmanager.data.repository.BatchRepository
import com.digifello.tuitionmanager.data.repository.PaymentRepository
import com.digifello.tuitionmanager.ui.common.UiState
import com.digifello.tuitionmanager.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class BatchWithStatus(
    val batch: Batch,
    val paymentStatus: String
)

class DashboardViewModel(
    private val batchRepository: BatchRepository = BatchRepository(),
    private val paymentRepository: PaymentRepository = PaymentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<BatchWithStatus>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<BatchWithStatus>>> = _uiState.asStateFlow()

    init {
        loadBatches()
    }

    private fun loadBatches() {
        viewModelScope.launch {
            // collectLatest: whenever the batch list itself changes, restart everything below
            batchRepository.getBatchesFlow().collectLatest { batches ->
                if (batches.isEmpty()) {
                    _uiState.value = UiState.Success(emptyList())
                    return@collectLatest
                }

                val month = DateUtils.currentMonthKey()

                // One LIVE payment listener per batch, instead of a one-time fetch —
                // this is what makes Dashboard update automatically the moment
                // a payment is marked on Batch Detail, without needing a restart.
                val paymentFlows = batches.map { batch ->
                    paymentRepository.getPaymentForMonth(batch.id, month)
                }

                combine(paymentFlows) { paymentsArray ->
                    batches.mapIndexed { index, batch ->
                        BatchWithStatus(batch, paymentsArray[index]?.status ?: "pending")
                    }
                }.collect { combinedList ->
                    _uiState.value = UiState.Success(combinedList)
                }
            }
        }
    }
}