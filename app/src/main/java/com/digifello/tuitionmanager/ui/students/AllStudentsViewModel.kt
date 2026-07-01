package com.digifello.tuitionmanager.ui.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digifello.tuitionmanager.data.model.Student
import com.digifello.tuitionmanager.data.repository.StudentRepository
import com.digifello.tuitionmanager.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AllStudentsViewModel(
    private val studentRepository: StudentRepository = StudentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Student>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Student>>> = _uiState.asStateFlow()

    // Keeps the full unfiltered list in memory so search can filter locally,
    // instead of hitting Firestore again on every keystroke
    private var allStudents: List<Student> = emptyList()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        viewModelScope.launch {
            studentRepository.getAllStudentsFlow().collect { students ->
                allStudents = students
                applyFilter()
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        applyFilter()
    }

    private fun applyFilter() {
        val query = _searchQuery.value.trim()
        val filtered = if (query.isEmpty()) {
            allStudents
        } else {
            allStudents.filter {
                it.name.contains(query, ignoreCase = true) || it.phone.contains(query)
            }
        }
        _uiState.value = UiState.Success(filtered)
    }
}