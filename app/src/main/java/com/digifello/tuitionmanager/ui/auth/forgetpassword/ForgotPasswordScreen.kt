package com.digifello.tuitionmanager.ui.auth.forgotpassword

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digifello.tuitionmanager.ui.common.AppPrimaryButton
import com.digifello.tuitionmanager.ui.common.AppTextActionButton
import com.digifello.tuitionmanager.ui.common.AppTextField
import com.digifello.tuitionmanager.ui.theme.ChalkWhite
import com.digifello.tuitionmanager.ui.theme.Ink60
import com.digifello.tuitionmanager.ui.theme.InkNavy
import com.digifello.tuitionmanager.ui.theme.Marigold
import com.digifello.tuitionmanager.ui.theme.PaidGreen
import com.digifello.tuitionmanager.ui.theme.PaidGreenBg
import com.digifello.tuitionmanager.ui.theme.SkyTint
import com.digifello.tuitionmanager.ui.theme.TuitionManagerTheme
import com.digifello.tuitionmanager.ui.theme.UnpaidCrimson

@Composable
fun ForgotPasswordScreen(
    onResetEmailSent: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    val state = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChalkWhite)
            .verticalScroll(rememberScrollState())
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

        if (!state.emailSent) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(SkyTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.MailOutline, contentDescription = null, tint = Marigold)
            }

            Text(
                text = "Forgot your password?",
                style = MaterialTheme.typography.headlineLarge,
                color = InkNavy,
                modifier = Modifier.padding(top = 20.dp)
            )
            Text(
                text = "Enter the email linked to your account and we'll send you a reset link.",
                style = MaterialTheme.typography.bodyMedium,
                color = Ink60,
                modifier = Modifier.padding(top = 6.dp, bottom = 32.dp)
            )

            AppTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = "Email",
                placeholder = "you@example.com",
                keyboardType = KeyboardType.Email,
                leadingIcon = { Icon(Icons.Filled.MailOutline, contentDescription = null, tint = Ink60) }
            )

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = UnpaidCrimson,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            AppPrimaryButton(
                text = "Send Reset Link",
                onClick = { viewModel.sendResetEmail(onSent = {}) },
                loading = state.isLoading
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(PaidGreenBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.MarkEmailRead,
                        contentDescription = null,
                        tint = PaidGreen,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "Check your inbox",
                    style = MaterialTheme.typography.headlineMedium,
                    color = InkNavy,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 20.dp)
                )
                Text(
                    text = "We've sent a password reset link to ${state.email}. Follow the link to set a new password.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink60,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )

                AppPrimaryButton(text = "Back to Login", onClick = onResetEmailSent)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            AppTextActionButton(text = "Back to login", onClick = onBackClick)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordScreenPreview() {
    TuitionManagerTheme {
        ForgotPasswordScreen(onResetEmailSent = {}, onBackClick = {})
    }
}