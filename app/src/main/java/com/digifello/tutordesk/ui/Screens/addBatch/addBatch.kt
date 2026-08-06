package com.digifello.tutordesk.ui.Screens.addBatch


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digifello.tutordesk.ui.theme.CardWhite
import com.digifello.tutordesk.ui.theme.CardWhiteDark
import com.digifello.tutordesk.ui.theme.ChalkWhite
import com.digifello.tutordesk.ui.theme.ChalkWhiteDark
import com.digifello.tutordesk.ui.theme.Ink_Navy
import com.digifello.tutordesk.ui.theme.Ink_NavyDark
import com.digifello.tutordesk.ui.theme.Marigold
import com.digifello.tutordesk.ui.theme.SlateBlue
import com.digifello.tutordesk.ui.theme.SlateBlueDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBatchScreen(
    onBatchCreated: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddBatchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) ChalkWhiteDark else ChalkWhite
    val cardColor = if (isDark) CardWhiteDark else CardWhite
    val textColor = if (isDark) Ink_NavyDark else Ink_Navy
    val secondaryColor = if (isDark) SlateBlueDark else SlateBlue

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onBatchCreated()
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Add Batch", color = textColor, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = uiState.batchName,
                    onValueChange = viewModel::onBatchNameChange,
                    label = { Text("Batch name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                Column {
                    Text(
                        text = "Class days",
                        style = MaterialTheme.typography.labelLarge,
                        color = secondaryColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowDayRow(
                        days = viewModel.weekdays,
                        selectedDays = uiState.selectedDays,
                        cardColor = cardColor,
                        textColor = textColor,
                        secondaryColor = secondaryColor,
                        onToggle = viewModel::toggleDay
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = uiState.time,
                    onValueChange = viewModel::onTimeChange,
                    label = { Text("Class time (e.g. 5:00 PM)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.totalMoneyText,
                    onValueChange = viewModel::onTotalMoneyChange,
                    label = { Text("Total monthly fee (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.studentCountText,
                    onValueChange = viewModel::onStudentCountChange,
                    label = { Text("Number of students") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            if (uiState.students.isNotEmpty()) {
                item {
                    Text(
                        text = "Student details",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (uiState.expectedAmountPerStudent > 0) {
                    item {
                        Text(
                            text = "₹${uiState.expectedAmountPerStudent} per student / month",
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondaryColor
                        )
                    }
                }
            }

            itemsIndexed(uiState.students) { index, student ->
                StudentInputCard(
                    index = index,
                    name = student.name,
                    phone = student.phone,
                    cardColor = cardColor,
                    textColor = textColor,
                    onNameChange = { viewModel.onStudentNameChange(index, it) },
                    onPhoneChange = { viewModel.onStudentPhoneChange(index, it) }
                )
            }

            if (uiState.errorMessage != null) {
                item {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                Button(
                    onClick = viewModel::submit,
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Marigold,
                        contentColor = Ink_Navy
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Ink_Navy,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Create Batch", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowDayRow(
    days: List<String>,
    selectedDays: Set<String>,
    cardColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    secondaryColor: androidx.compose.ui.graphics.Color,
    onToggle: (String) -> Unit
) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        days.forEach { day ->
            val isSelected = selectedDays.contains(day)
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) Marigold else cardColor)
                    .clickable { onToggle(day) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    color = if (isSelected) Ink_Navy else secondaryColor,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun StudentInputCard(
    index: Int,
    name: String,
    phone: String,
    cardColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Student ${index + 1}",
                style = MaterialTheme.typography.labelLarge,
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = { Text("Phone (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
        }
    }
}