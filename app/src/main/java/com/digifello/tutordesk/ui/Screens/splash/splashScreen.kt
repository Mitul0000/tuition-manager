package com.digifello.tutordesk.ui.Screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.digifello.tutordesk.R
import com.digifello.tutordesk.data.repository.AuthRepository
import com.digifello.tutordesk.ui.theme.ChalkWhite
import com.digifello.tutordesk.ui.theme.ChalkWhiteDark
import com.digifello.tutordesk.ui.theme.Marigold
import kotlinx.coroutines.delay

@Composable
fun Splashscreen(
    onNavigationToOnboarding: () -> Unit,
    onNavigationToLogin: () -> Unit,
    onNavigationToEmailVerification: () -> Unit,
    onNavigationToMain: () -> Unit,
    authRepository: AuthRepository = AuthRepository()
) {
    val context = LocalContext.current
    val onboardingPrefs = remember { OnboardingPreferences(context) }

    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) ChalkWhiteDark else ChalkWhite

    LaunchedEffect(Unit) {
        delay(400)

        if (!onboardingPrefs.hasSeenOnboarding) {
            onNavigationToOnboarding()
            return@LaunchedEffect
        }

        authRepository.reloadUser()
        val user = authRepository.currentUserId

        when {
            user == null -> onNavigationToLogin()
            !authRepository.isEmailVerified -> onNavigationToEmailVerification()
            else -> {
                AppMaintenance().runMonthlyRolloverIfNeeded()
                onNavigationToMain()
            }
        }
    }

    Scaffold(
        containerColor = backgroundColor
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.logo_app),
                    contentDescription = "TutorDesk logo",
                    modifier = Modifier.size(100.dp)
                        .clip(shape = RoundedCornerShape(20.dp))
                )
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(color = Marigold)
            }
        }
    }
}