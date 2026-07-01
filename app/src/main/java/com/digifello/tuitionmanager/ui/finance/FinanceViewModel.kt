package com.digifello.tuitionmanager.ui.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digifello.tuitionmanager.data.repository.BatchRepository
import com.digifello.tuitionmanager.data.repository.PaymentRepository
import com.digifello.tuitionmanager.ui.common.UiState
import com.digifello.tuitionmanager.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DueItem(
    val batchId: String,
    val batchName: String,
    val month: String,
    val dueAmount: Double
)

data class BatchMonthStatus(
    val batchId: String,
    val batchName: String,
    val status: String,
    val amountPaid: Double,
    val expectedAmount: Double
)

data class FinanceUiState(
    val expectedThisMonth: Double = 0.0,
    val receivedThisMonth: Double = 0.0,
    val totalDue: Double = 0.0,
    val dueItems: List<DueItem> = emptyList(),
    val batchBreakdown: List<BatchMonthStatus> = emptyList()
)

class FinanceViewModel(
    private val batchRepository: BatchRepository = BatchRepository(),
    private val paymentRepository: PaymentRepository = PaymentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<FinanceUiState>>(UiState.Loading)
    val uiState: StateFlow<UiState<FinanceUiState>> = _uiState.asStateFlow()

    init {
        loadFinanceData()
    }

    fun loadFinanceData() {
        viewModelScope.launch {
            val currentMonth = DateUtils.currentMonthKey()
            val batches = batchRepository.getBatchesOnce()
            val batchIds = batches.map { it.id }

            // "This Month" — only current month's records, summed
            val currentMonthPayments = paymentRepository.getCurrentMonthPayments(batchIds, currentMonth)
            val expected = currentMonthPayments.sumOf { it.expectedAmount }
            val received = currentMonthPayments.sumOf { it.amountPaid }

            // "Total Due" — every unpaid/partial record, any month (dues persist across months)
            val outstanding = paymentRepository.getAllOutstandingPayments()
            val dueItems = outstanding
                .map { DueItem(it.batchId, it.batchName, it.month, it.expectedAmount - it.amountPaid) }
                .filter { it.dueAmount > 0.0 }
            val totalDue = dueItems.sumOf { it.dueAmount }

            // Batch-wise breakdown — this month's status per batch, for the bottom list
            val breakdown = currentMonthPayments.map {
                BatchMonthStatus(it.batchId, it.batchName, it.status, it.amountPaid, it.expectedAmount)
            }

            _uiState.value = UiState.Success(
                FinanceUiState(
                    expectedThisMonth = expected,
                    receivedThisMonth = received,
                    totalDue = totalDue,
                    dueItems = dueItems,
                    batchBreakdown = breakdown
                )
            )
        }
    }
}