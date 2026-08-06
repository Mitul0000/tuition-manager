package com.digifello.tutordesk.ui.Screens.SavedPaperScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digifello.tutordesk.data.model.SavedPaper
import com.digifello.tutordesk.data.repository.QuestionGeneratorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SavedPapersUiState(
    val isLoading: Boolean = false,
    val papers: List<SavedPaper> = emptyList(),
    val errorMessage: String? = null
)

class SavedPapersViewModel(
    private val repository: QuestionGeneratorRepository = QuestionGeneratorRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedPapersUiState())
    val uiState: StateFlow<SavedPapersUiState> = _uiState.asStateFlow()

    init {
        loadPapers()
    }

    fun loadPapers() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val response = repository.getMyPapers()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    papers = response.papers
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load saved papers"
                )
            }
        }
    }
}