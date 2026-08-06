package com.digifello.tutordesk.ui.Screens.auth.loginScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.*
import com.digifello.tutordesk.R
import com.digifello.tutordesk.ui.theme.CardWhite
import com.digifello.tutordesk.ui.theme.CardWhiteDark
import com.digifello.tutordesk.ui.theme.ChalkWhite
import com.digifello.tutordesk.ui.theme.ChalkWhiteDark
import com.digifello.tutordesk.ui.theme.Ink_Navy
import com.digifello.tutordesk.ui.theme.Ink_NavyDark
import com.digifello.tutordesk.ui.theme.Marigold
import com.digifello.tutordesk.ui.theme.SlateBlue
import com.digifello.tutordesk.ui.theme.SlateBlueDark
import com.digifello.tutordesk.ui.theme.UnpaidCrimson

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoginSuccessful) {
        if (uiState.isLoginSuccessful) onLoginSuccess()
    }

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
        focusedTrailingIconColor = textColor,
        unfocusedTrailingIconColor = secondaryColor,
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

            Spacer(modifier = Modifier.height(28.dp))

            val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(R.raw.login_charecter)
            )

            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(250.dp)
            )

            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium,
                color = textColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Pick up right where you left off",
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryColor,
                textAlign = TextAlign.Center
            )

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = uiState.errorMessage ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = UnpaidCrimson,
                    textAlign = TextAlign.Center
                )
            }

            if (uiState.needsEmailVerification) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Please verify your email before logging in.",
                    style = MaterialTheme.typography.bodySmall,
                    color = UnpaidCrimson,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "EMAIL ADDRESS",
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; viewModel.clearError() },
                placeholder = { Text("name@tutordesk.com") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = uiState.errorMessage != null,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "PASSWORD",
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; viewModel.clearError() },
                placeholder = { Text("••••••••") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                isError = uiState.errorMessage != null,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onNavigateToForgotPassword) {
                    Text(
                        text = "forgot password?",
                        style = MaterialTheme.typography.labelSmall,
                        color = Marigold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.login(email, password) },
                enabled = !uiState.isLoading,
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
                        text = "Login",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink_Navy
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = secondaryColor.copy(alpha = 0.3f))
                Text(
                    text = "  NEW TO TUTORDESK?  ",
                    style = MaterialTheme.typography.labelSmall,
                    color = secondaryColor
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = secondaryColor.copy(alpha = 0.3f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = onNavigateToSignup,
                border = BorderStroke(1.dp, textColor.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Create Your Account",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}