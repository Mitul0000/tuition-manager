package com.digifello.tutordesk.ui.Screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.digifello.tutordesk.ui.theme.CardWhite
import com.digifello.tutordesk.ui.theme.CardWhiteDark
import com.digifello.tutordesk.ui.theme.ChalkWhite
import com.digifello.tutordesk.ui.theme.ChalkWhiteDark
import com.digifello.tutordesk.ui.theme.Ink_Navy
import com.digifello.tutordesk.ui.theme.Ink_NavyDark
import com.digifello.tutordesk.ui.theme.Marigold
import com.digifello.tutordesk.ui.theme.SlateBlue
import com.digifello.tutordesk.ui.theme.SlateBlueDark

@Composable
fun MoreScreen(
    modifier: Modifier = Modifier,
    viewModel: MoreViewModel = MoreViewModel(),
    onSearchStudentClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onSavedPapersClick: () -> Unit = {},
    onLoggedOut: () -> Unit = {}
) {
    val uiState = viewModel.uiState

    val isDark = isSystemInDarkTheme()
    val cardColor = if (isDark) CardWhiteDark else CardWhite
    val textColor = if (isDark) Ink_NavyDark else Ink_Navy
    val secondaryColor = if (isDark) SlateBlueDark else SlateBlue

    var showLogoutConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Marigold),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.userName.firstOrNull()?.uppercaseChar()?.toString() ?: "T",
                        color = Ink_Navy,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = uiState.userName,
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (uiState.userEmail.isNotBlank()) {
                        Text(
                            text = uiState.userEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionLabel("Students", secondaryColor)
        MoreRow(
            icon = Icons.Default.PersonSearch,
            label = "Search Student",
            cardColor = cardColor,
            textColor = textColor,
            secondaryColor = secondaryColor,
            onClick = onSearchStudentClick
        )

        Spacer(modifier = Modifier.height(20.dp))

        SectionLabel("Papers", secondaryColor)
        MoreRow(
            icon = Icons.Default.Description,
            label = "Saved Papers",
            cardColor = cardColor,
            textColor = textColor,
            secondaryColor = secondaryColor,
            onClick = onSavedPapersClick
        )

        Spacer(modifier = Modifier.height(20.dp))

        SectionLabel("Account", secondaryColor)
        MoreRow(
            icon = Icons.Default.Lock,
            label = "Change Password",
            cardColor = cardColor,
            textColor = textColor,
            secondaryColor = secondaryColor,
            onClick = onChangePasswordClick
        )
        MoreRow(
            icon = Icons.Default.Logout,
            label = "Log Out",
            cardColor = cardColor,
            textColor = MaterialTheme.colorScheme.error,
            secondaryColor = secondaryColor,
            onClick = { showLogoutConfirm = true }
        )

        Spacer(modifier = Modifier.height(20.dp))

        SectionLabel("Support", secondaryColor)
        MoreRow(
            icon = Icons.Default.HelpOutline,
            label = "Help & FAQ",
            cardColor = cardColor,
            textColor = textColor,
            secondaryColor = secondaryColor,
            onClick = onHelpClick
        )
        MoreRow(
            icon = Icons.Default.Description,
            label = "Terms & Privacy",
            cardColor = cardColor,
            textColor = textColor,
            secondaryColor = secondaryColor,
            onClick = onPrivacyClick
        )
        MoreRow(
            icon = Icons.Default.Info,
            label = "About TutorDesk",
            cardColor = cardColor,
            textColor = textColor,
            secondaryColor = secondaryColor,
            onClick = onAboutClick
        )
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Log out?") },
            text = { Text("You'll need to sign in again to access your batches and students.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    viewModel.logout()
                    onLoggedOut()
                }) { Text("Log Out", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SectionLabel(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
private fun MoreRow(
    icon: ImageVector,
    label: String,
    cardColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    secondaryColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = textColor)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = secondaryColor)
        }
    }
}