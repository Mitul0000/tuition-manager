package com.digifello.tutordesk.ui.Screens.auth.verify

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.*
import com.digifello.tutordesk.R
import com.digifello.tutordesk.data.repository.AuthRepository
import com.digifello.tutordesk.ui.Screens.auth.verify.EmailVerificationViewModel
import com.digifello.tutordesk.ui.theme.ChalkWhite
import com.digifello.tutordesk.ui.theme.ChalkWhiteDark
import com.digifello.tutordesk.ui.theme.Ink_Navy
import com.digifello.tutordesk.ui.theme.Ink_NavyDark
import com.digifello.tutordesk.ui.theme.Marigold
import com.digifello.tutordesk.ui.theme.SlateBlue
import com.digifello.tutordesk.ui.theme.SlateBlueDark
import com.digifello.tutordesk.ui.theme.UnpaidCrimson

@Composable
fun EmailVerificationScreen(
    onVerified: () -> Unit,
    onGoBack: () -> Unit,
    viewModel: EmailVerificationViewModel = viewModel(),
    auth: AuthRepository = AuthRepository()
) {
    val userEmail = auth.currentUserEmail?:""
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isVerified) {
        if (uiState.isVerified) onVerified()
    }

    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) ChalkWhiteDark else ChalkWhite
    val textColor = if (isDark) Ink_NavyDark else Ink_Navy
    val secondaryColor = if (isDark) SlateBlueDark else SlateBlue

    Scaffold(containerColor = backgroundColor) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(R.raw.verify)
            )

            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Verify Your Email",
                style = MaterialTheme.typography.headlineMedium,
                color = textColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "We've sent a confirmation link to your email",
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = userEmail,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = secondaryColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Open the email and tap the verification link, then come back here.",
                style = MaterialTheme.typography.bodySmall,
                color = secondaryColor,
                textAlign = TextAlign.Center
            )

            if (uiState.verificationFailed) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "We couldn't confirm verification yet. Make sure you've clicked the link, then try again.",
                    style = MaterialTheme.typography.bodySmall,
                    color = UnpaidCrimson,
                    textAlign = TextAlign.Center
                )
            }

            if (uiState.resendMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.resendMessage ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryColor,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Primary: "I've Verified My Email" — triggers reload() + isEmailVerified check
            Button(
                onClick = { viewModel.checkVerification() },
                enabled = !uiState.isChecking,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Marigold,
                    contentColor = Ink_Navy,
                    disabledContainerColor = Marigold.copy(alpha = 0.5f),
                    disabledContentColor = Ink_Navy.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uiState.isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Ink_Navy,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MarkEmailRead,
                        contentDescription = null,
                        tint = Ink_Navy
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "I've Verified My Email",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink_Navy
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Secondary: resend the verification email
            OutlinedButton(
                onClick = { viewModel.resendEmail() },
                enabled = !uiState.isResending,
                border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uiState.isResending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = textColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Resend Email",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = {
                viewModel.logout()
                onGoBack()
            }) {
                Text(
                    text = "Wrong email? Go back",
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryColor
                )
            }
        }
    }
}