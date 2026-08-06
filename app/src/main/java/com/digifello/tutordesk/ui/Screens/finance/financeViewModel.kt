package com.digifello.tutordesk.ui.Screens.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digifello.tutordesk.data.model.Batch
import com.digifello.tutordesk.data.model.Payment
import com.digifello.tutordesk.data.model.Student
import com.digifello.tutordesk.data.repository.BatchRepository
import com.digifello.tutordesk.data.repository.PaymentRepository
import com.digifello.tutordesk.data.repository.StudentRepository
import com.digifello.tutordesk.util.currentMonthKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class BatchFinanceRow(
    val batch: Batch,
    val expected: Int,
    val collected: Int
)

data class FinanceUiState(
    val isLoading: Boolean = true,
    val currentMonth: String = currentMonthKey(),
    val expectedThisMonth: Int = 0,
    val collectedThisMonth: Int = 0,
    val totalEarnedAllTime: Long = 0L,
    val batchRows: List<BatchFinanceRow> = emptyList(),
    val errorMessage: String? = null
) {
    val pendingThisMonth: Int get() = (expectedThisMonth - collectedThisMonth).coerceAtLeast(0)
    val collectionProgress: Float
        get() = if (expectedThisMonth > 0) (collectedThisMonth.toFloat() / expectedThisMonth).coerceIn(0f, 1f) else 0f
}

class FinanceViewModel(
    private val batchRepository: BatchRepository = BatchRepository(),
    private val studentRepository: StudentRepository = StudentRepository(),
    private val paymentRepository: PaymentRepository = PaymentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    private var latestBatches: List<Batch> = emptyList()
    private var latestStudents: List<Student> = emptyList()
    private var latestPayments: Map<String, Payment?> = emptyMap()

    private var paymentsJob: Job? = null

    init {
        observeBatches()
        observeStudents()
        observeTotalEarned()
    }

    private fun observeBatches() {
        viewModelScope.launch {
            batchRepository.getBatches()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Could not load finance data."
                    )
                }
                .collect { batches ->
                    latestBatches = batches
                    recomputeBreakdown()
                }
        }
    }

    private fun observeStudents() {
        viewModelScope.launch {
            studentRepository.getStudents()
                .catch { }
                .collect { students ->
                    latestStudents = students
                    observePaymentsForCurrentStudents()
                    recomputeBreakdown()
                }
        }
    }

    private fun observePaymentsForCurrentStudents() {
        paymentsJob?.cancel()
        paymentsJob = viewModelScope.launch {
            paymentRepository.observePaymentsForStudents(
                studentIds = latestStudents.map { it.id },
                month = _uiState.value.currentMonth
            )
                .catch { }
                .collect { payments ->
                    latestPayments = payments
                    recomputeBreakdown()
                }
        }
    }

    private fun recomputeBreakdown() {
        val batches = latestBatches
        val students = latestStudents
        val payments = latestPayments

        val rows = batches.map { batch ->
            val collected = students
                .filter { it.batchId == batch.id }
                .sumOf { payments[it.id]?.amountPaid ?: 0 }

            BatchFinanceRow(
                batch = batch,
                expected = batch.totalMoney,
                collected = collected
            )
        }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            batchRows = rows,
            expectedThisMonth = rows.sumOf { it.expected },
            collectedThisMonth = rows.sumOf { it.collected },
            errorMessage = null
        )
    }

    private fun observeTotalEarned() {
        viewModelScope.launch {
            paymentRepository.getTotalEarnedAllTime()
                .catch { }
                .collect { total ->
                    _uiState.value = _uiState.value.copy(totalEarnedAllTime = total)
                }
        }
    }
}