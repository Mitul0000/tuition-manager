package com.digifello.tutordesk.ui.Screens.batchDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digifello.tutordesk.data.model.Batch
import com.digifello.tutordesk.data.model.Payment
import com.digifello.tutordesk.data.model.Student
import com.digifello.tutordesk.data.repository.BatchRepository
import com.digifello.tutordesk.data.repository.PaymentRepository
import com.digifello.tutordesk.data.repository.StudentRepository
import com.digifello.tutordesk.util.currentMonthKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class BatchDetailsUiState(
    val isLoading: Boolean = true,
    val batch: Batch? = null,
    val students: List<Student> = emptyList(),

    val isEditingBatch: Boolean = false,
    val editName: String = "",
    val editDays: Set<String> = emptySet(),
    val editTime: String = "",
    val editTotalMoney: String = "",

    val isAddingStudent: Boolean = false,
    val newStudentName: String = "",
    val newStudentPhone: String = "",

    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isBatchDeleted: Boolean = false,
    val updateErrorMessage: String? = null,

    val currentMonth: String = currentMonthKey(),
    val paymentsByStudentId: Map<String, Payment?> = emptyMap(),
    val studentForPaymentDialog: Student? = null,
    val paymentAmountText: String = "",
    val showMarkBatchPaidConfirm: Boolean = false
)

class BatchDetailsViewModel(
    private val batchId: String,
    private val batchRepository: BatchRepository = BatchRepository(),
    private val studentRepository: StudentRepository = StudentRepository(),
    private val paymentRepository: PaymentRepository = PaymentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(BatchDetailsUiState())
    val uiState: StateFlow<BatchDetailsUiState> = _uiState.asStateFlow()

    init {
        observeBatch()
        observeStudents()
    }

    private fun observeBatch() {
        viewModelScope.launch {
            batchRepository.getBatch(batchId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Could not load batch."
                    )
                }
                .collect { batch ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        batch = batch
                    )
                }
        }
    }

    private fun observeStudents() {
        viewModelScope.launch {
            studentRepository.getStudentsForBatch(batchId)
                .catch { }
                .collect { students ->
                    _uiState.value = _uiState.value.copy(students = students)
                    refreshPayments(students)
                }
        }
    }

    private var paymentsFetchId = 0

    private fun refreshPayments(students: List<Student>) {
        val requestId = ++paymentsFetchId

        if (students.isEmpty()) {
            _uiState.value = _uiState.value.copy(paymentsByStudentId = emptyMap())
            return
        }

        viewModelScope.launch {
            val payments = paymentRepository.getPaymentsForStudents(
                studentIds = students.map { it.id },
                month = _uiState.value.currentMonth
            )
            if (requestId == paymentsFetchId) {
                _uiState.value = _uiState.value.copy(paymentsByStudentId = payments)
            }
        }
    }

    fun startEditingBatch() {
        val batch = _uiState.value.batch ?: return
        _uiState.value = _uiState.value.copy(
            isEditingBatch = true,
            editName = batch.name,
            editDays = batch.days.toSet(),
            editTime = batch.time,
            editTotalMoney = batch.totalMoney.toString()
        )
    }

    fun cancelEditingBatch() {
        _uiState.value = _uiState.value.copy(isEditingBatch = false, errorMessage = null)
    }

    fun onEditNameChange(value: String) {
        _uiState.value = _uiState.value.copy(editName = value)
    }

    fun toggleEditDay(day: String) {
        val current = _uiState.value.editDays
        val updated = if (current.contains(day)) current - day else current + day
        _uiState.value = _uiState.value.copy(editDays = updated)
    }

    fun onEditTimeChange(value: String) {
        _uiState.value = _uiState.value.copy(editTime = value)
    }

    fun onEditTotalMoneyChange(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(editTotalMoney = value)
        }
    }

    fun saveBatchEdits() {
        val state = _uiState.value
        val batch = state.batch ?: return

        if (state.editName.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Batch name is required")
            return
        }
        if (state.editDays.isEmpty()) {
            _uiState.value = state.copy(errorMessage = "Select at least one day")
            return
        }
        val total = state.editTotalMoney.toIntOrNull()
        if (total == null || total <= 0) {
            _uiState.value = state.copy(errorMessage = "Enter a valid total fee")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            try {
                batchRepository.updateBatch(
                    batch.copy(
                        name = state.editName.trim(),
                        days = state.editDays.toList(),
                        time = state.editTime.trim(),
                        totalMoney = total
                    )
                )
                _uiState.value = _uiState.value.copy(isSaving = false, isEditingBatch = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    updateErrorMessage = e.message ?: "Could not save changes."
                )
            }
        }
    }

    fun deleteBatch() {
        val batch = _uiState.value.batch ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            try {
                _uiState.value = _uiState.value.copy(isSaving = false, isBatchDeleted = true)
                batchRepository.deleteBatch(batch)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Could not delete batch."
                )
            }
        }
    }

    fun startAddingStudent() {
        _uiState.value = _uiState.value.copy(
            isAddingStudent = true,
            newStudentName = "",
            newStudentPhone = "",
            errorMessage = null
        )
    }

    fun cancelAddingStudent() {
        _uiState.value = _uiState.value.copy(isAddingStudent = false)
    }

    fun onNewStudentNameChange(value: String) {
        _uiState.value = _uiState.value.copy(newStudentName = value)
    }

    fun onNewStudentPhoneChange(value: String) {
        _uiState.value = _uiState.value.copy(newStudentPhone = value)
    }

    fun addStudent() {
        val state = _uiState.value
        val batch = state.batch ?: return

        if (state.newStudentName.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Student name is required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            try {
                val newStudent = Student(
                    name = state.newStudentName.trim(),
                    phone = state.newStudentPhone.trim(),
                    batchName = batch.name
                )

                val studentId = studentRepository.createStudent(
                    student = newStudent,
                    batchId = batch.id
                )

                if (studentId.isNotBlank()) {
                    val newCount = state.students.size + 1
                    val expectedAmount = if (newCount > 0) batch.totalMoney / newCount else 0

                    paymentRepository.seedMonthForStudent(
                        studentId = studentId,
                        month = currentMonthKey(),
                        expectedAmount = expectedAmount
                    )

                    paymentRepository.updatePaymentForAllStudent(batch.id, expectedAmount)

                    val updatedStudent = newStudent.copy(id = studentId, batchId = batch.id)
                    refreshPayments(state.students + updatedStudent)
                }

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isAddingStudent = false,
                    newStudentName = "",
                    newStudentPhone = ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Could not add student."
                )
            }
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            try {
                studentRepository.deleteStudent(student)
                _uiState.value = _uiState.value.copy(isSaving = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Could not delete student."
                )
            }
        }
    }

    fun openPaymentDialog(student: Student) {
        val existing = _uiState.value.paymentsByStudentId[student.id]
        _uiState.value = _uiState.value.copy(
            studentForPaymentDialog = student,
            paymentAmountText = (existing?.amountPaid ?: 0).toString(),
            errorMessage = null
        )
    }

    fun closePaymentDialog() {
        _uiState.value = _uiState.value.copy(studentForPaymentDialog = null, paymentAmountText = "")
    }

    fun onPaymentAmountChange(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(paymentAmountText = value)
        }
    }

    fun submitStudentPayment() {
        val state = _uiState.value
        val student = state.studentForPaymentDialog ?: return
        val amount = state.paymentAmountText.toIntOrNull()

        if (amount == null || amount < 0) {
            _uiState.value = state.copy(errorMessage = "Enter a valid amount")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            try {
                paymentRepository.markPaymentByStudent(
                    studentId = student.id,
                    month = state.currentMonth,
                    newAmountPaid = amount
                )
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    studentForPaymentDialog = null,
                    paymentAmountText = ""
                )
                refreshPayments(_uiState.value.students)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Could not save payment."
                )
            }
        }
    }

    fun requestMarkBatchPaid() {
        _uiState.value = _uiState.value.copy(showMarkBatchPaidConfirm = true)
    }

    fun cancelMarkBatchPaid() {
        _uiState.value = _uiState.value.copy(showMarkBatchPaidConfirm = false)
    }

    fun confirmMarkBatchPaid() {
        _uiState.value = _uiState.value.copy(showMarkBatchPaidConfirm = false)
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            try {
                paymentRepository.markPaymentByBatch(
                    batchId = batchId,
                    month = _uiState.value.currentMonth
                )
                _uiState.value = _uiState.value.copy(isSaving = false)
                refreshPayments(_uiState.value.students)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Could not mark batch as paid."
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}