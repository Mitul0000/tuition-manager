package com.digifello.tuitionmanager.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val isEmailVerified: Boolean = false,
    val isSaving: Boolean = false,
    val isSendingReset: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class ProfileViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    var uiState by mutableStateOf(loadInitialState())
        private set

    private fun loadInitialState(): ProfileUiState {
        val user = auth.currentUser
        return ProfileUiState(
            name = user?.displayName.orEmpty(),
            email = user?.email.orEmpty(),
            isEmailVerified = user?.isEmailVerified == true
        )
    }

    fun onNameChange(value: String) {
        uiState = uiState.copy(name = value, errorMessage = null, infoMessage = null)
    }

    fun saveName() {
        if (uiState.name.isBlank()) {
            uiState = uiState.copy(errorMessage = "Name can't be empty")
            return
        }
        uiState = uiState.copy(isSaving = true, errorMessage = null, infoMessage = null)
        viewModelScope.launch {
            try {
                val request = UserProfileChangeRequest.Builder()
                    .setDisplayName(uiState.name.trim())
                    .build()
                auth.currentUser?.updateProfile(request)?.await()
                uiState = uiState.copy(isSaving = false, infoMessage = "Profile updated")
            } catch (e: Exception) {
                uiState = uiState.copy(isSaving = false, errorMessage = e.message ?: "Could not update profile")
            }
        }
    }

    fun sendPasswordReset() {
        val email = uiState.email
        if (email.isBlank()) return
        uiState = uiState.copy(isSendingReset = true, errorMessage = null, infoMessage = null)
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                uiState = uiState.copy(isSendingReset = false, infoMessage = "Password reset email sent")
            } catch (e: Exception) {
                uiState = uiState.copy(isSendingReset = false, errorMessage = e.message ?: "Could not send reset email")
            }
        }
    }
}