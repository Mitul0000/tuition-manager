package com.digifello.tuitionmanager.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

// The ONLY identity system in this app. No custom backend accounts —
// Firebase Auth handles login/signup/OTP/password reset directly.
// Wraps the Firebase SDK in suspend functions + Result so ViewModels
// don't touch FirebaseAuth callbacks directly.

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    val currentUserId: String?
        get() = auth.currentUser?.uid

    val isLoggedIn: Boolean
        get() = auth.currentUser != null

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Login failed. Please check your credentials.")
        }
    }

    suspend fun signup(email: String, password: String): AuthResult {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            // Sends Firebase's built-in verification email — acts as our
            // "OTP" step without us needing to write any OTP logic ourselves.
            auth.currentUser?.sendEmailVerification()?.await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Could not create account.")
        }
    }

    suspend fun sendPasswordResetEmail(email: String): AuthResult {
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

    val isEmailVerified: Boolean
        get() = auth.currentUser?.isEmailVerified == true

    suspend fun getIdToken(forceRefresh: Boolean = false): String? {
        return try {
            auth.currentUser?.getIdToken(forceRefresh)?.await()?.token
        } catch (e: Exception) {
            null
        }
    }

    fun logout() {
        auth.signOut()
    }
}