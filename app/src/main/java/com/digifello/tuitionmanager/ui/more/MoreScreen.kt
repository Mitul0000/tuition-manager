package com.digifello.tuitionmanager.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.digifello.tuitionmanager.ui.common.AppCard
import com.digifello.tuitionmanager.ui.theme.ChalkWhite
import com.digifello.tuitionmanager.ui.theme.Ink40
import com.digifello.tuitionmanager.ui.theme.Ink60
import com.digifello.tuitionmanager.ui.theme.InkNavy
import com.digifello.tuitionmanager.ui.theme.Marigold
import com.digifello.tuitionmanager.ui.theme.SkyTint
import com.digifello.tuitionmanager.ui.theme.TuitionManagerTheme
import com.digifello.tuitionmanager.ui.theme.UnpaidCrimson
import com.digifello.tuitionmanager.ui.theme.UnpaidCrimsonBg
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MoreScreen(
    onStudentsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLoggedOut: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val user = FirebaseAuth.getInstance().currentUser
    val displayName = user?.displayName?.takeIf { it.isNotBlank() } ?: "TutorDesk User"
    val email = user?.email ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChalkWhite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "More",
            style = MaterialTheme.typography.headlineLarge,
            color = InkNavy,
            modifier = Modifier.padding(top = 24.dp, bottom = 20.dp)
        )

        AppCard(onClick = onProfileClick, modifier = Modifier.padding(bottom = 24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(SkyTint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = Marigold)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = InkNavy,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (email.isNotBlank()) {
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink60
                        )
                    }
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Ink40)
            }
        }

        Text(
            text = "MANAGE",
            style = MaterialTheme.typography.labelMedium,
            color = Ink40,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        MoreRow(
            icon = Icons.Filled.Groups,
            label = "All Students",
            subtitle = "View and manage every student",
            onClick = onStudentsClick
        )
        Spacer(modifier = Modifier.height(12.dp))
        MoreRow(
            icon = Icons.Filled.Description,
            label = "Settings",
            subtitle = "App preferences",
            onClick = onSettingsClick
        )

        Spacer(modifier = Modifier.height(32.dp))

        MoreRow(
            icon = Icons.AutoMirrored.Filled.Logout,
            label = "Log Out",
            subtitle = "Sign out of this account",
            danger = true,
            onClick = { showLogoutDialog = true }
        )

        Spacer(modifier = Modifier.height(40.dp))
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log out?", style = MaterialTheme.typography.titleLarge, color = InkNavy) },
            text = {
                Text(
                    "You'll need to log in again to access your batches and papers.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink60
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    FirebaseAuth.getInstance().signOut()
                    showLogoutDialog = false
                    onLoggedOut()
                }) {
                    Text("Log Out", color = UnpaidCrimson, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Ink60)
                }
            }
        )
    }
}

@Composable
private fun MoreRow(
    icon: ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
    danger: Boolean = false
) {
    val iconTint = if (danger) UnpaidCrimson else Marigold
    val iconBg = if (danger) UnpaidCrimsonBg else SkyTint
    val labelColor = if (danger) UnpaidCrimson else InkNavy

    AppCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium, color = labelColor)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Ink60)
            }
            if (!danger) {
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Ink40)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MoreScreenPreview() {
    TuitionManagerTheme {
        MoreScreen(onStudentsClick = {}, onProfileClick = {}, onSettingsClick = {}, onLoggedOut = {})
    }
}