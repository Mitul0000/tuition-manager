package com.digifello.tutordesk.ui.Screens.auth.verify
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digifello.tutordesk.data.repository.AuthRepository
import com.digifello.tutordesk.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EmailVerificationUiState(
    val isChecking: Boolean = false,
    val verificationFailed: Boolean = false,
    val isVerified: Boolean = false,
    val isResending: Boolean = false,
    val resendMessage: String? = null
)

class EmailVerificationViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmailVerificationUiState())
    val uiState: StateFlow<EmailVerificationUiState> = _uiState.asStateFlow()

    fun checkVerification() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isChecking = true, verificationFailed = false)

            authRepository.reloadUser()

            if (authRepository.isEmailVerified) {
                _uiState.value = _uiState.value.copy(isChecking = false, isVerified = true)
            } else {
                _uiState.value = _uiState.value.copy(isChecking = false, verificationFailed = true)
            }
        }
    }

    fun resendEmail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isResending = true, resendMessage = null)

            when (val result = authRepository.resendVerificationEmail()) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isResending = false,
                        resendMessage = "Verification email resent."
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isResending = false,
                        resendMessage = result.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isResending = false)
                }
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}