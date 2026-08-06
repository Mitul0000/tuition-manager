package com.digifello.tutordesk.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.rpc.context.AttributeContext
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    object Success : AuthResult()
    object EmailNotVerified: AuthResult()
    data class Error(val message: String): AuthResult()
}

class AuthRepository(private val auth : FirebaseAuth = FirebaseAuth.getInstance()) {
    val currentUserId: String? get() = auth.currentUser?.uid
    val isLoggedIn: Boolean get() = currentUserId != null
    val currentUserName: String? get() = auth.currentUser?.displayName
    val currentUserEmail: String? get() = auth.currentUser?.email


    suspend fun login(email: String, password: String): AuthResult {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            auth.currentUser?.reload()?.await()

            if (auth.currentUser?.isEmailVerified == false) {
                AuthResult.EmailNotVerified
            } else {
                AuthResult.Success
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Login failed. Please check your credentials.")
        }
    }

    suspend fun signup(
        fullName: String,
        email: String,
        password: String
    ): AuthResult {
        return try {

            auth.createUserWithEmailAndPassword(email, password).await()

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build()

            auth.currentUser?.updateProfile(profileUpdates)?.await()

            auth.currentUser?.sendEmailVerification()?.await()

            AuthResult.Success

        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Could not create account.")
        }
    }

    suspend fun forgetPassword(email: String): AuthResult {
        return try {
            auth.sendPasswordResetEmail(email).await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Could not send reset email.")
        }
    }

    suspend fun reloadUser(): AuthResult {
        return try {
            auth.currentUser?.reload()?.await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Could not refresh account status.")
        }
    }

    val isEmailVerified: Boolean get() = auth.currentUser?.isEmailVerified == true

    suspend fun getIdToken(forceRefresh: Boolean = false): String? {
        return try {
            auth.currentUser?.getIdToken(forceRefresh)?.await()?.token
        } catch (e: Exception) {
            null
        }
    }

    suspend fun resendVerificationEmail(): AuthResult {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Could not resend verification email.")
        }
    }

    fun logout() {
        auth.signOut()
    }
}