package com.digifello.tuitionmanager.ui.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digifello.tuitionmanager.data.repository.AppMaintenance
import com.digifello.tuitionmanager.ui.theme.ChalkWhite
import com.digifello.tuitionmanager.ui.theme.InkNavy
import com.digifello.tuitionmanager.ui.theme.Marigold
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

// First screen shown on launch. Checks whether a Firebase session already
// exists and routes onward — no UI decision-making happens here beyond
// a short branded pause, so the check feels intentional rather than a flash.

@Composable
fun SplashScreen(
    onAuthenticated: () -> Unit,
    onUnauthenticated: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "splash_alpha"
    )

    LaunchedEffect(Unit) {
        delay(600) // brief branded pause, avoids a jarring instant redirect
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Real session confirmed — safe to run the monthly payment
            // reset check now (see AppMaintenance for the logic).
            AppMaintenance().runMonthlyPaymentMaintenance()
            onAuthenticated()
        } else {
            onUnauthenticated()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "TutorDesk",
            style = MaterialTheme.typography.displayLarge,
            color = InkNavy,
            modifier = Modifier.alpha(alpha)
        )
        Text(
            text = "Tuition Manager & Question Generator",
            style = MaterialTheme.typography.bodyMedium,
            color = Marigold,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}