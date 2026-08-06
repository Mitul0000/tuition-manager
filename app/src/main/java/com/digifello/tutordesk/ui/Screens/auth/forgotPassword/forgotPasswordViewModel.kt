package com.digifello.tutordesk.ui.Screens.auth.forgotPassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digifello.tutordesk.data.repository.AuthRepository
import com.digifello.tutordesk.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ForgetPassUiState(
    var isLoading : Boolean = false,
    var emailSent : Boolean = false,
    var errorMessage: String? = null
)

class ForgotPasswordViewModel (
    private val authRepository: AuthRepository = AuthRepository()
): ViewModel(){
    private val _uiState = MutableStateFlow(ForgetPassUiState())
    val uiState: StateFlow<ForgetPassUiState> = _uiState.asStateFlow()

    fun forgotPassword(email: String){
        _uiState.value = _uiState.value.copy(
            isLoading = true
        )
        viewModelScope.launch {
            when(val result = authRepository.forgetPassword(email)){
                is AuthResult.Success ->{
                    _uiState.value = _uiState.value.copy(
                        emailSent = true,
                        isLoading = false
                    )
                }
                is AuthResult.Error->{
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                else ->{
                    _uiState.value = _uiState.value.copy(
                        isLoading = false
                    )
                }

            }
        }
    }
    fun clearMessage(){
        if(_uiState.value.errorMessage != null){
            _uiState.value = _uiState.value.copy(
                errorMessage = null
            )
        }
    }

}