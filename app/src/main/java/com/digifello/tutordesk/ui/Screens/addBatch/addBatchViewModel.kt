package com.digifello.tutordesk.ui.Screens.addBatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digifello.tutordesk.data.model.Batch
import com.digifello.tutordesk.data.model.Student
import com.digifello.tutordesk.data.repository.BatchRepository
import com.digifello.tutordesk.data.repository.PaymentRepository
import com.digifello.tutordesk.data.repository.StudentRepository
import com.digifello.tutordesk.ui.Screens.routine.WEEKDAYS
import com.digifello.tutordesk.util.currentMonthKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StudentInput(
    val name: String = "",
    val phone: String = ""
)

data class AddBatchUiState(
    val batchName: String = "",
    val selectedDays: Set<String> = emptySet(),
    val time: String = "",
    val totalMoneyText: String = "",
    val studentCountText: String = "",
    val students: List<StudentInput> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
) {
    val expectedAmountPerStudent: Int
        get() {
            val total = totalMoneyText.toIntOrNull() ?: return 0
            val count = students.size
            return if (count > 0) total / count else 0
        }
}

class AddBatchViewModel(
    private val batchRepository: BatchRepository = BatchRepository(),
    private val studentRepository: StudentRepository = StudentRepository(),
    private val paymentRepository: PaymentRepository = PaymentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddBatchUiState())
    val uiState: StateFlow<AddBatchUiState> = _uiState.asStateFlow()

    val weekdays = WEEKDAYS

    fun onBatchNameChange(value: String) {
        _uiState.value = _uiState.value.copy(batchName = value, errorMessage = null)
    }

    fun toggleDay(day: String) {
        val current = _uiState.value.selectedDays
        val updated = if (current.contains(day)) current - day else current + day
        _uiState.value = _uiState.value.copy(selectedDays = updated, errorMessage = null)
    }

    fun onTimeChange(value: String) {
        _uiState.value = _uiState.value.copy(time = value, errorMessage = null)
    }

    fun onTotalMoneyChange(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(totalMoneyText = value, errorMessage = null)
        }
    }

    fun onStudentCountChange(value: String) {
        if (value.isNotEmpty() && !value.all { it.isDigit() }) return

        val count = value.toIntOrNull() ?: 0
        val cappedCount = count.coerceIn(0, 200) // sane upper bound

        val currentStudents = _uiState.value.students
        val resized = when {
            cappedCount == currentStudents.size -> currentStudents
            cappedCount < currentStudents.size -> currentStudents.take(cappedCount)
            else -> currentStudents + List(cappedCount - currentStudents.size) { StudentInput() }
        }

        _uiState.value = _uiState.value.copy(
            studentCountText = value,
            students = resized,
            errorMessage = null
        )
    }

    fun onStudentNameChange(index: Int, name: String) {
        updateStudentAt(index) { it.copy(name = name) }
    }

    fun onStudentPhoneChange(index: Int, phone: String) {
        updateStudentAt(index) { it.copy(phone = phone) }
    }

    private fun updateStudentAt(index: Int, transform: (StudentInput) -> StudentInput) {
        val students = _uiState.value.students.toMutableList()
        if (index !in students.indices) return
        students[index] = transform(students[index])
        _uiState.value = _uiState.value.copy(students = students, errorMessage = null)
    }

    fun submit() {
        val state = _uiState.value

        val validationError = validate(state)
        if (validationError != null) {
            _uiState.value = state.copy(errorMessage = validationError)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

            try {
                val totalMoney = state.totalMoneyText.toInt()
                val expectedAmount = totalMoney / state.students.size
                val month = currentMonthKey()

                val batchId = batchRepository.createBatch(
                    Batch(
                        name = state.batchName.trim(),
                        days = state.selectedDays.toList(),
                        time = state.time.trim(),
                        totalMoney = totalMoney,
                        studentCount = 0
                    )
                )

                if (batchId.isBlank()) {
                    _uiState.value = state.copy(
                        isSaving = false,
                        errorMessage = "Could not create batch. Please try again."
                    )
                    return@launch
                }

                for (studentInput in state.students) {
                    val studentId = studentRepository.createStudent(
                        student = Student(
                            name = studentInput.name.trim(),
                            phone = studentInput.phone.trim(),
                            batchName = state.batchName.trim()
                        ),
                        batchId = batchId
                    )

                    if (studentId.isNotBlank()) {
                        paymentRepository.seedMonthForStudent(
                            studentId = studentId,
                            month = month,
                            expectedAmount = expectedAmount
                        )
                    }
                }

                _uiState.value = AddBatchUiState(isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Something went wrong while saving."
                )
            }
        }
    }

    private fun validate(state: AddBatchUiState): String? {
        if (state.batchName.isBlank()) return "Batch name is required"
        if (state.selectedDays.isEmpty()) return "Select at least one day"
        if (state.time.isBlank()) return "Class time is required"
        val total = state.totalMoneyText.toIntOrNull()
        if (total == null || total <= 0) return "Enter a valid total fee"
        if (state.students.isEmpty()) return "Number of students must be at least 1"
        val emptyStudentIndex = state.students.indexOfFirst { it.name.isBlank() }
        if (emptyStudentIndex != -1) return "Enter a name for student #${emptyStudentIndex + 1}"
        return null
    }
}