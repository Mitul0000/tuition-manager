package com.digifello.tuitionmanager.ui.addeditbatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digifello.tuitionmanager.data.model.Batch
import com.digifello.tuitionmanager.data.model.Student
import com.digifello.tuitionmanager.data.repository.BatchRepository
import com.digifello.tuitionmanager.data.repository.PaymentRepository
import com.digifello.tuitionmanager.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StudentFormEntry(
    val name: String = "",
    val phone: String = "",
    val description: String = ""
)

data class AddEditBatchFormState(
    val batchName: String = "",
    val numberOfStudents: String = "",
    val totalMoney: String = "",
    val selectedDays: Set<String> = emptySet(),
    val time: String = "",
    val studentEntries: List<StudentFormEntry> = emptyList(),
    val isSaving: Boolean = false,
    val isLoadingExisting: Boolean = false,
    val errorMessage: String? = null
)

class AddEditBatchViewModel(
    private val existingBatchId: String? = null,
    private val batchRepository: BatchRepository = BatchRepository(),
    private val paymentRepository: PaymentRepository = PaymentRepository()
) : ViewModel() {

    val isEditMode: Boolean = existingBatchId != null

    private val _formState = MutableStateFlow(AddEditBatchFormState(isLoadingExisting = isEditMode))
    val formState: StateFlow<AddEditBatchFormState> = _formState.asStateFlow()

    init {
        if (existingBatchId != null) {
            loadExistingBatch(existingBatchId)
        }
    }

    private fun loadExistingBatch(batchId: String) {
        viewModelScope.launch {
            val batch = batchRepository.getBatch(batchId)
            if (batch != null) {
                _formState.value = _formState.value.copy(
                    batchName = batch.name,
                    numberOfStudents = batch.numberOfStudents.toString(),
                    totalMoney = batch.totalMoney.toString(),
                    selectedDays = batch.days.toSet(),
                    time = batch.time,
                    isLoadingExisting = false
                )
            } else {
                _formState.value = _formState.value.copy(
                    isLoadingExisting = false,
                    errorMessage = "Could not load batch details"
                )
            }
        }
    }

    fun onBatchNameChanged(value: String) {
        _formState.value = _formState.value.copy(batchName = value)
    }

    fun onNumberOfStudentsChanged(value: String) {
        _formState.value = _formState.value.copy(numberOfStudents = value)
    }

    fun onTotalMoneyChanged(value: String) {
        _formState.value = _formState.value.copy(totalMoney = value)
    }

    fun onDayToggled(day: String) {
        val current = _formState.value.selectedDays
        val updated = if (current.contains(day)) current - day else current + day
        _formState.value = _formState.value.copy(selectedDays = updated)
    }

    fun onTimeChanged(value: String) {
        _formState.value = _formState.value.copy(time = value)
    }

    fun generateStudentEntries() {
        val count = _formState.value.numberOfStudents.toIntOrNull() ?: 0
        _formState.value = _formState.value.copy(
            studentEntries = List(count) { StudentFormEntry() }
        )
    }

    fun onStudentFieldChanged(index: Int, updated: StudentFormEntry) {
        val entries = _formState.value.studentEntries.toMutableList()
        if (index in entries.indices) {
            entries[index] = updated
            _formState.value = _formState.value.copy(studentEntries = entries)
        }
    }

    /**
     * Handles both create and update. In edit mode, only batch-level
     * fields are saved — the roster is managed separately (not yet built),
     * so numberOfStudents/students are NOT touched here when editing.
     *
     * For NEW batches, this also immediately creates the current month's
     * payment record, instead of waiting for the next app-open maintenance
     * pass — otherwise a freshly created batch shows no payment status
     * until the app is restarted.
     */
    fun saveBatch(onSaved: () -> Unit) {
        val state = _formState.value
        val totalMoney = state.totalMoney.toDoubleOrNull()
        val numberOfStudents = state.numberOfStudents.toIntOrNull()

        if (state.batchName.isBlank() || totalMoney == null || numberOfStudents == null || state.selectedDays.isEmpty()) {
            _formState.value = state.copy(errorMessage = "Please fill in all required fields")
            return
        }

        _formState.value = state.copy(isSaving = true, errorMessage = null)

        viewModelScope.launch {
            try {
                if (isEditMode && existingBatchId != null) {
                    val updatedBatch = Batch(
                        id = existingBatchId,
                        name = state.batchName,
                        days = state.selectedDays.toList(),
                        time = state.time,
                        numberOfStudents = numberOfStudents,
                        totalMoney = totalMoney
                    )
                    batchRepository.updateBatch(updatedBatch)
                } else {
                    val newBatch = Batch(
                        name = state.batchName,
                        days = state.selectedDays.toList(),
                        time = state.time,
                        numberOfStudents = numberOfStudents,
                        totalMoney = totalMoney
                    )
                    val students = state.studentEntries.map {
                        Student(name = it.name, phone = it.phone, description = it.description.ifBlank { null })
                    }
                    val newBatchId = batchRepository.addBatch(newBatch, students)

                    paymentRepository.ensureCurrentMonthPayment(
                        batchId = newBatchId,
                        batchName = state.batchName,
                        month = DateUtils.currentMonthKey(),
                        expectedAmount = totalMoney
                    )
                }
                _formState.value = _formState.value.copy(isSaving = false)
                onSaved()
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    isSaving = false,
                    errorMessage = "Failed to save: ${e.message}"
                )
            }
        }
    }
}