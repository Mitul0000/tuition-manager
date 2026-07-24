package com.digifello.tuitionmanager.ui.students.studentdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digifello.tuitionmanager.data.model.Payment
import com.digifello.tuitionmanager.data.model.Student
import com.digifello.tuitionmanager.data.repository.PaymentRepository
import com.digifello.tuitionmanager.data.repository.StudentRepository
import com.digifello.tuitionmanager.ui.common.UiState
import com.digifello.tuitionmanager.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StudentDetailUiState(
    val student: Student? = null,
    val currentPayment: Payment? = null,
    val isDeleting: Boolean = false
)

class StudentDetailViewModel(
    private val studentId: String,
    private val studentRepository: StudentRepository = StudentRepository(),
    private val paymentRepository: PaymentRepository = PaymentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<StudentDetailUiState>>(UiState.Loading)
    val uiState: StateFlow<UiState<StudentDetailUiState>> = _uiState.asStateFlow()

    private val currentMonth = DateUtils.currentMonthKey()

    init {
        viewModelScope.launch {
            studentRepository.getStudentByIdFlow(studentId).collect { student ->
                if (student == null) {
                    _uiState.value = UiState.Error("Student not found")
                    return@collect
                }
                loadPaymentFor(student)
            }
        }
    }

    private fun loadPaymentFor(student: Student) {
        viewModelScope.launch {
            paymentRepository.getPaymentForMonth(student.batchId, currentMonth).collect { payment ->
                val current = (_uiState.value as? UiState.Success)?.data
                _uiState.value = UiState.Success(
                    (current ?: StudentDetailUiState()).copy(student = student, currentPayment = payment)
                )
            }
        }
    }

    fun updateStudent(name: String, phone: String, description: String?, onDone: () -> Unit) {
        val student = (_uiState.value as? UiState.Success)?.data?.student ?: return
        viewModelScope.launch {
            val updated = student.copy(name = name, phone = phone, description = description)
            studentRepository.updateStudent(student.batchId, updated)
            onDone()
        }
    }

    fun deleteStudent(onDeleted: () -> Unit) {
        val student = (_uiState.value as? UiState.Success)?.data?.student ?: return
        val current = _uiState.value
        if (current is UiState.Success) {
            _uiState.value = UiState.Success(current.data.copy(isDeleting = true))
        }
        viewModelScope.launch {
            studentRepository.deleteStudent(student.batchId, student.id)
            onDeleted()
        }
    }
}