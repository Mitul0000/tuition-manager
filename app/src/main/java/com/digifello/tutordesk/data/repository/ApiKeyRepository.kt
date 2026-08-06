package com.digifello.tutordesk.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class ApiKeyRepository(context: Context) {

    private val appContext = context.applicationContext

    private val masterKey by lazy {
        MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val prefs by lazy {
        EncryptedSharedPreferences.create(
            appContext,
            "secure_api_keys",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getGeminiApiKey(): String? = prefs.getString(KEY_GEMINI_API_KEY, null)

    fun saveGeminiApiKey(key: String) {
        prefs.edit().putString(KEY_GEMINI_API_KEY, key.trim()).apply()
    }

    fun clearGeminiApiKey() {
        prefs.edit().remove(KEY_GEMINI_API_KEY).apply()
    }

    fun hasApiKey(): Boolean = !getGeminiApiKey().isNullOrBlank()

    companion object {
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
    }
}