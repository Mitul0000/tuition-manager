package com.digifello.tutordesk.ui.Screens.more

import androidx.lifecycle.ViewModel
import com.digifello.tutordesk.data.repository.AuthRepository

data class MoreUiState(
    val userName: String,
    val userEmail: String
)

class MoreViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    val uiState: MoreUiState
        get() = MoreUiState(
            userName = authRepository.currentUserName?.takeIf { it.isNotBlank() } ?: "Tutor",
            userEmail = authRepository.currentUserEmail ?: ""
        )

    fun logout() {
        authRepository.logout()
    }
}