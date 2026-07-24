package com.digifello.tuitionmanager.ui.auth.otp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digifello.tuitionmanager.data.repository.AuthRepository
import com.digifello.tuitionmanager.data.repository.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

data class OtpUiState(
    val isChecking: Boolean = false,
    val isResending: Boolean = false,
    val isVerified: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class OtpViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    var uiState by mutableStateOf(OtpUiState())
        private set

    fun checkVerification(onVerified: () -> Unit) {
        uiState = uiState.copy(isChecking = true, errorMessage = null, infoMessage = null)
        viewModelScope.launch {
            when (val result = authRepository.reloadUser()) {
                is AuthResult.Success -> {
                    if (authRepository.isEmailVerified) {
                        uiState = uiState.copy(isChecking = false, isVerified = true)
                        onVerified()
                    } else {
                        uiState = uiState.copy(
                            isChecking = false,
                            infoMessage = "Not verified yet — check your inbox and tap the link."
                        )
                    }
                }
                is AuthResult.Error -> {
                    uiState = uiState.copy(isChecking = false, errorMessage = result.message)
                }
            }
        }
    }

    fun resendEmail() {
        uiState = uiState.copy(isResending = true, errorMessage = null, infoMessage = null)
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
                uiState = uiState.copy(isResending = false, infoMessage = "Verification email resent.")
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isResending = false,
                    errorMessage = e.message ?: "Could not resend email."
                )
            }
        }
    }
}