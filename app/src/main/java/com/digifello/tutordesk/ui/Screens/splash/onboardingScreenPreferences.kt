package com.digifello.tutordesk.ui.Screens.splash

import android.content.Context
import android.content.SharedPreferences

class OnboardingPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("tutordesk_prefs", Context.MODE_PRIVATE)

    var hasSeenOnboarding: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_SEEN, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_SEEN, value).apply()

    companion object {
        private const val KEY_ONBOARDING_SEEN = "has_seen_onboarding"
    }
}