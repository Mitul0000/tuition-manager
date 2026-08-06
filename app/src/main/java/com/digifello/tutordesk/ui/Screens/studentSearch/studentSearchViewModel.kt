package com.digifello.tutordesk.ui.Screens.studentSearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digifello.tutordesk.data.model.Student
import com.digifello.tutordesk.data.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class StudentSearchUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val allStudents: List<Student> = emptyList(),
    val errorMessage: String? = null
) {
    // getStudents() already returns every student regardless of active/batch
    // status, so a deleted-batch student still shows up here — matches the
    // requirement that search covers "any student added till now".
    val filteredStudents: List<Student>
        get() = if (query.isBlank()) {
            allStudents
        } else {
            allStudents.filter { it.name.contains(query, ignoreCase = true) }
        }
}

class StudentSearchViewModel(
    private val studentRepository: StudentRepository = StudentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentSearchUiState())
    val uiState: StateFlow<StudentSearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            studentRepository.getStudents()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Could not load students."
                    )
                }
                .collect { students ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        allStudents = students
                    )
                }
        }
    }

    fun onQueryChange(value: String) {
        _uiState.value = _uiState.value.copy(query = value)
    }
}