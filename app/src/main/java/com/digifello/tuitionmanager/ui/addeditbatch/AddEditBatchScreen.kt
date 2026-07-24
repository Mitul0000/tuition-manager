package com.digifello.tuitionmanager.ui.addeditbatch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.digifello.tuitionmanager.ui.common.AppPrimaryButton
import com.digifello.tuitionmanager.ui.common.AppTextField
import com.digifello.tuitionmanager.ui.theme.ChalkWhite
import com.digifello.tuitionmanager.ui.theme.Ink60
import com.digifello.tuitionmanager.ui.theme.InkNavy
import com.digifello.tuitionmanager.ui.theme.Marigold
import com.digifello.tuitionmanager.ui.theme.UnpaidCrimson

private val ALL_DAYS = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBatchScreen(
    viewModel: AddEditBatchViewModel,
    onSaved: () -> Unit,
    onBackClick: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    var step by remember { mutableStateOf(1) }

    Scaffold(
        containerColor = ChalkWhite,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (viewModel.isEditMode) "Edit Batch" else "New Batch",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = InkNavy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ChalkWhite,
                    titleContentColor = InkNavy
                )
            )
        }
    ) { padding ->
        if (formState.isLoadingExisting) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Marigold)
            }
            return@Scaffold
        }

        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {

            // Edit mode skips the roster step entirely — only batch fields are editable.
            // Roster management is a separate future feature (see mockup's "Manage Student List").
            if (viewModel.isEditMode || step == 1) {
                Step1BatchFields(formState, viewModel)
                Spacer(Modifier.height(16.dp))
                formState.errorMessage?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = UnpaidCrimson)
                    Spacer(Modifier.height(8.dp))
                }

                if (viewModel.isEditMode) {
                    AppPrimaryButton(
                        text = if (formState.isSaving) "Saving..." else "Save Changes",
                        onClick = { viewModel.saveBatch(onSaved) },
                        enabled = !formState.isSaving,
                        loading = formState.isSaving
                    )
                } else {
                    AppPrimaryButton(
                        text = "Continue to Roster",
                        onClick = {
                            viewModel.generateStudentEntries()
                            step = 2
                        },
                        enabled = formState.batchName.isNotBlank() &&
                                formState.numberOfStudents.toIntOrNull() != null &&
                                formState.totalMoney.toDoubleOrNull() != null &&
                                formState.selectedDays.isNotEmpty()
                    )
                }
            } else {
                Step2StudentRoster(formState, viewModel)
                Spacer(Modifier.height(16.dp))
                formState.errorMessage?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = UnpaidCrimson)
                    Spacer(Modifier.height(8.dp))
                }
                AppPrimaryButton(
                    text = if (formState.isSaving) "Saving..." else "Save Batch",
                    onClick = { viewModel.saveBatch(onSaved) },
                    enabled = !formState.isSaving,
                    loading = formState.isSaving
                )
            }
        }
    }
}

@Composable
private fun Step1BatchFields(formState: AddEditBatchFormState, viewModel: AddEditBatchViewModel) {
    AppTextField(
        value = formState.batchName,
        onValueChange = { viewModel.onBatchNameChanged(it) },
        label = "Batch Name",
        placeholder = "e.g. Grade 10 Geometry"
    )
    Spacer(Modifier.height(16.dp))

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        AppTextField(
            value = formState.numberOfStudents,
            onValueChange = { viewModel.onNumberOfStudentsChanged(it) },
            label = "No. of Students",
            keyboardType = KeyboardType.Number,
            enabled = !viewModel.isEditMode, // roster count locked once created; managed separately later
            modifier = Modifier.weight(1f)
        )
        AppTextField(
            value = formState.totalMoney,
            onValueChange = { viewModel.onTotalMoneyChanged(it) },
            label = "Total Fee (৳)",
            keyboardType = KeyboardType.Number,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(20.dp))

    Text("Weekly Schedule", style = MaterialTheme.typography.titleSmall, color = InkNavy)
    Spacer(Modifier.height(8.dp))
    FlowRowDays(formState.selectedDays) { day -> viewModel.onDayToggled(day) }
    Spacer(Modifier.height(16.dp))

    AppTextField(
        value = formState.time,
        onValueChange = { viewModel.onTimeChanged(it) },
        label = "Class Time",
        placeholder = "e.g. 17:00"
    )
}

@Composable
private fun FlowRowDays(selectedDays: Set<String>, onToggle: (String) -> Unit) {
    Column {
        ALL_DAYS.chunked(4).forEach { rowDays ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowDays.forEach { day ->
                    val selected = selectedDays.contains(day)
                    FilterChip(
                        selected = selected,
                        onClick = { onToggle(day) },
                        label = { Text(day.take(3)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Marigold,
                            selectedLabelColor = InkNavy,
                            labelColor = Ink60
                        )
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Step2StudentRoster(formState: AddEditBatchFormState, viewModel: AddEditBatchViewModel) {
    Text("Student Roster", style = MaterialTheme.typography.titleMedium, color = InkNavy)
    Spacer(Modifier.height(12.dp))
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(formState.studentEntries.size) { index ->
            val entry = formState.studentEntries[index]
            Column {
                Text("Student ${index + 1}", style = MaterialTheme.typography.labelLarge, color = Ink60)
                Spacer(Modifier.height(6.dp))
                AppTextField(
                    value = entry.name,
                    onValueChange = { viewModel.onStudentFieldChanged(index, entry.copy(name = it)) },
                    label = "Name"
                )
                Spacer(Modifier.height(8.dp))
                AppTextField(
                    value = entry.phone,
                    onValueChange = { viewModel.onStudentFieldChanged(index, entry.copy(phone = it)) },
                    label = "Phone number",
                    keyboardType = KeyboardType.Phone
                )
                Spacer(Modifier.height(8.dp))
                AppTextField(
                    value = entry.description,
                    onValueChange = { viewModel.onStudentFieldChanged(index, entry.copy(description = it)) },
                    label = "Short description (optional)"
                )
            }
        }
    }
}