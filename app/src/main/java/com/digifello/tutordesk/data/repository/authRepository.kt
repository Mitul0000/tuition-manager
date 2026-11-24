package com.digifello.tutordesk.data.repository
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.digifello.tutordesk.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlin.coroutines.cancellation.CancellationException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await


sealed class AuthResult {
    object Success : AuthResult()
    object EmailNotVerified: AuthResult()
    data class Error(val message: String): AuthResult()
}

class AuthRepository(private val auth : FirebaseAuth = FirebaseAuth.getInstance()) {
    val currentUserId: String? get() = auth.currentUser?.uid
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

    suspend fun signInWithGoogle(context: Context): AuthResult {
        return try {
            val option = GetSignInWithGoogleOption.Builder(
                context.getString(R.string.default_web_client_id)
            ).build()
            val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

            val credential = CredentialManager.create(context)
                .getCredential(context, request).credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
                AuthResult.Success   // Google accounts are already verified, so skip email verification
            } else {
                AuthResult.Error("Unexpected credential type.")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.e("GoogleSignIn", "CANCELLED type=${e.type} msg=${e.message}", e)
            AuthResult.Error("Google sign-in cancelled.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Google sign-in failed.")
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