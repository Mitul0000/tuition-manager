package com.digifello.tuitionmanager.ui.auth.otp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digifello.tuitionmanager.ui.common.AppPrimaryButton
import com.digifello.tuitionmanager.ui.common.AppTextActionButton
import com.digifello.tuitionmanager.ui.theme.ChalkWhite
import com.digifello.tuitionmanager.ui.theme.Ink60
import com.digifello.tuitionmanager.ui.theme.InkNavy
import com.digifello.tuitionmanager.ui.theme.Marigold
import com.digifello.tuitionmanager.ui.theme.SkyTint
import com.digifello.tuitionmanager.ui.theme.TuitionManagerTheme
import com.digifello.tuitionmanager.ui.theme.UnpaidCrimson

@Composable
fun OtpScreen(
    email: String,
    purpose: String,
    onVerified: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: OtpViewModel = viewModel()
) {
    val state = viewModel.uiState

    val title = if (purpose == "signup") "Verify your email" else "Confirm it's you"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChalkWhite)
            .padding(horizontal = 28.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = InkNavy)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(SkyTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.MarkEmailUnread,
                    contentDescription = null,
                    tint = Marigold,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = InkNavy,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 20.dp)
            )
            Text(
                text = "We sent a verification link to $email. Open it, then come back and tap \"I've verified\".",
                style = MaterialTheme.typography.bodyMedium,
                color = Ink60,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )

            if (state.infoMessage != null) {
                Text(
                    text = state.infoMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = Marigold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = UnpaidCrimson,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            AppPrimaryButton(
                text = "I've verified",
                onClick = { viewModel.checkVerification(onVerified = onVerified) },
                loading = state.isChecking
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Didn't get it?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink60
                )
                AppTextActionButton(
                    text = if (state.isResending) "Sending…" else "Resend email",
                    onClick = { viewModel.resendEmail() }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OtpScreenPreview() {
    TuitionManagerTheme {
        OtpScreen(email = "you@example.com", purpose = "signup", onVerified = {}, onBackClick = {})
    }
}