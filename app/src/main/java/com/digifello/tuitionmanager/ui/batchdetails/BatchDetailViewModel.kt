package com.digifello.tuitionmanager.ui.batchdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digifello.tuitionmanager.data.model.Batch
import com.digifello.tuitionmanager.data.model.Payment
import com.digifello.tuitionmanager.data.model.Student
import com.digifello.tuitionmanager.data.repository.BatchRepository
import com.digifello.tuitionmanager.data.repository.PaymentRepository
import com.digifello.tuitionmanager.data.repository.StudentRepository
import com.digifello.tuitionmanager.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BatchDetailUiState(
    val batch: Batch? = null,
    val students: List<Student> = emptyList(),
    val currentPayment: Payment? = null,
    val isLoading: Boolean = true
)

class BatchDetailViewModel(
    private val batchId: String,
    private val batchRepository: BatchRepository = BatchRepository(),
    private val studentRepository: StudentRepository = StudentRepository(),
    private val paymentRepository: PaymentRepository = PaymentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(BatchDetailUiState())
    val uiState: StateFlow<BatchDetailUiState> = _uiState.asStateFlow()

    private val currentMonth = DateUtils.currentMonthKey()

    init {
        loadBatch()
        loadStudents()
        loadCurrentPayment()
    }

    private fun loadBatch() {
        viewModelScope.launch {
            val batch = batchRepository.getBatch(batchId)
            _uiState.value = _uiState.value.copy(batch = batch, isLoading = false)
        }
    }

    private fun loadStudents() {
        viewModelScope.launch {
            studentRepository.getStudentsForBatch(batchId).collect { students ->
                _uiState.value = _uiState.value.copy(students = students)
            }
        }
    }

    private fun loadCurrentPayment() {
        viewModelScope.launch {
            paymentRepository.getPaymentForMonth(batchId, currentMonth).collect { payment ->
                _uiState.value = _uiState.value.copy(currentPayment = payment)
            }
        }
    }

    /** Called when the "Mark Payment" dialog's Confirm button is tapped. */
    fun markPayment(amountPaid: Double) {
        val batch = _uiState.value.batch ?: return
        viewModelScope.launch {
            paymentRepository.updatePaymentAmount(
                batchId = batchId,
                batchName = batch.name,     // fixed: now passing batchName as PaymentRepository requires
                month = currentMonth,
                amountPaid = amountPaid,
                expectedAmount = batch.totalMoney
            )
        }
    }

    fun deleteBatch(onDeleted: () -> Unit) {
        viewModelScope.launch {
            batchRepository.deleteBatch(batchId)
            onDeleted()
        }
    }
}