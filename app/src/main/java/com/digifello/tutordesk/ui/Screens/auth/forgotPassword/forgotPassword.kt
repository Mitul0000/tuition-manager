package com.digifello.tutordesk.ui.Screens.auth.forgotPassword

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digifello.tutordesk.ui.theme.ChalkWhite
import com.digifello.tutordesk.ui.theme.ChalkWhiteDark
import com.digifello.tutordesk.ui.theme.Ink_Navy
import com.digifello.tutordesk.ui.theme.Ink_NavyDark
import com.digifello.tutordesk.ui.theme.Marigold
import com.digifello.tutordesk.ui.theme.SlateBlue
import com.digifello.tutordesk.ui.theme.SlateBlueDark
import com.digifello.tutordesk.ui.theme.UnpaidCrimson

@Composable
fun ForgotPasswordScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }

    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) ChalkWhiteDark else ChalkWhite
    val textColor = if (isDark) Ink_NavyDark else Ink_Navy
    val secondaryColor = if (isDark) SlateBlueDark else SlateBlue

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = textColor,
        unfocusedTextColor = textColor,
        focusedBorderColor = Marigold,
        unfocusedBorderColor = secondaryColor.copy(alpha = 0.5f),
        focusedLeadingIconColor = textColor,
        unfocusedLeadingIconColor = secondaryColor,
        cursorColor = Marigold,
        focusedPlaceholderColor = secondaryColor,
        unfocusedPlaceholderColor = secondaryColor,
        errorBorderColor = UnpaidCrimson,
        errorTextColor = textColor
    )

    Scaffold(containerColor = backgroundColor) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Marigold.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (uiState.emailSent) Icons.Default.MarkEmailRead else Icons.Default.LockReset,
                    contentDescription = null,
                    tint = Marigold,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (!uiState.emailSent) {
                // ---------- STATE 1: enter email ----------
                Text(
                    text = "Forgot Password?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No worries — enter the email linked to your account and we'll send you a reset link",
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = "EMAIL ADDRESS",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; viewModel.clearMessage() },
                    placeholder = { Text("name@tutordesk.com") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    isError = uiState.errorMessage != null,
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = UnpaidCrimson,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = { viewModel.forgotPassword(email) },
                    enabled = email.isNotBlank() && !uiState.isLoading,
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
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Ink_Navy,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Send Reset Link",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Ink_Navy
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = "Back to Login",
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }

            } else {
                // ---------- STATE 2: confirmation ----------
                Text(
                    text = "Check Your Inbox",
                    style = MaterialTheme.typography.headlineMedium,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "We've sent a password reset link to",
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Tap the link in that email to set a new password, then come back and log in.",
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onNavigateToLogin,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Marigold,
                        contentColor = Ink_Navy
                    ),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = "Back to Login",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink_Navy
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { viewModel.forgotPassword(email) }) {
                    Text(
                        text = "Didn't get it? Resend",
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}