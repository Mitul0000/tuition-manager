package com.digifello.tutordesk.ui.Screens.auth.SignupScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digifello.tutordesk.data.repository.AuthRepository
import com.digifello.tutordesk.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SignupUiState(
    val isLoading: Boolean = false,
    val errMessage: String? = null,
    val isSignUpSuccessful: Boolean = false
)

class SignupViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    fun signup(
        fullname: String,
        email: String,
        password: String
    ) {
        _uiState.value = _uiState.value.copy(
            isLoading = true
        )
        viewModelScope.launch {
            when (val result = authRepository.signup(fullname, email, password)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSignUpSuccessful = true
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errMessage = result.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false
                    )
                }
            }
        }
    }

    fun clearError() {
        if (_uiState.value.errMessage != null) {
            _uiState.value = _uiState.value.copy(
                errMessage = null
            )
        }
    }
}