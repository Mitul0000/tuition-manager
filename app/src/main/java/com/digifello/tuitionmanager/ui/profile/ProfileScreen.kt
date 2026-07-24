package com.digifello.tuitionmanager.ui.profile

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digifello.tuitionmanager.ui.common.AppCard
import com.digifello.tuitionmanager.ui.common.AppPrimaryButton
import com.digifello.tuitionmanager.ui.common.AppSecondaryButton
import com.digifello.tuitionmanager.ui.common.AppTextField
import com.digifello.tuitionmanager.ui.theme.ChalkWhite
import com.digifello.tuitionmanager.ui.theme.Ink60
import com.digifello.tuitionmanager.ui.theme.InkNavy
import com.digifello.tuitionmanager.ui.theme.Marigold
import com.digifello.tuitionmanager.ui.theme.PaidGreen
import com.digifello.tuitionmanager.ui.theme.PaidGreenBg
import com.digifello.tuitionmanager.ui.theme.PartialAmber
import com.digifello.tuitionmanager.ui.theme.PartialAmberBg
import com.digifello.tuitionmanager.ui.theme.SkyTint
import com.digifello.tuitionmanager.ui.theme.TuitionManagerTheme
import com.digifello.tuitionmanager.ui.theme.UnpaidCrimson

@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val state = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChalkWhite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
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
            Text(
                text = "Profile",
                style = MaterialTheme.typography.titleLarge,
                color = InkNavy,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(SkyTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = Marigold,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        AppCard(modifier = Modifier.padding(bottom = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (state.isEmailVerified) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (state.isEmailVerified) PaidGreen else PartialAmber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = state.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkNavy
                    )
                    Text(
                        text = if (state.isEmailVerified) "Email verified" else "Email not verified",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state.isEmailVerified) PaidGreen else PartialAmber
                    )
                }
            }
        }

        Text(
            text = "Full name",
            style = MaterialTheme.typography.labelMedium,
            color = Ink60,
            modifier = Modifier.padding(bottom = 6.dp, top = 8.dp)
        )
        AppTextField(
            value = state.name,
            onValueChange = viewModel::onNameChange,
            label = "Name",
            placeholder = "Your name"
        )

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = UnpaidCrimson,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
        if (state.infoMessage != null) {
            Text(
                text = state.infoMessage,
                style = MaterialTheme.typography.bodySmall,
                color = PaidGreen,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        AppPrimaryButton(
            text = "Save Changes",
            onClick = { viewModel.saveName() },
            loading = state.isSaving
        )

        Spacer(modifier = Modifier.height(12.dp))

        AppSecondaryButton(
            text = if (state.isSendingReset) "Sending…" else "Change Password",
            onClick = { viewModel.sendPasswordReset() }
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    TuitionManagerTheme {
        ProfileScreen(onBackClick = {})
    }
}