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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val ALL_DAYS = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

@Composable
fun AddEditBatchScreen(
    viewModel: AddEditBatchViewModel,
    onSaved: () -> Unit,
    onBackClick: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    var step by remember { mutableStateOf(1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditMode) "Edit Batch" else "Batch Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (formState.isLoadingExisting) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
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
                    Text(it, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                }

                if (viewModel.isEditMode) {
                    Button(
                        onClick = { viewModel.saveBatch(onSaved) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !formState.isSaving
                    ) {
                        Text(if (formState.isSaving) "Saving..." else "Save Changes")
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.generateStudentEntries()
                            step = 2
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = formState.batchName.isNotBlank() &&
                                formState.numberOfStudents.toIntOrNull() != null &&
                                formState.totalMoney.toDoubleOrNull() != null &&
                                formState.selectedDays.isNotEmpty()
                    ) {
                        Text("Continue to Roster")
                    }
                }
            } else {
                Step2StudentRoster(formState, viewModel)
                Spacer(Modifier.height(16.dp))
                formState.errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                }
                Button(
                    onClick = { viewModel.saveBatch(onSaved) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !formState.isSaving
                ) {
                    Text(if (formState.isSaving) "Saving..." else "Save Batch")
                }
            }
        }
    }
}

@Composable
private fun Step1BatchFields(formState: AddEditBatchFormState, viewModel: AddEditBatchViewModel) {
    Text("Batch Name", fontWeight = FontWeight.Bold)
    OutlinedTextField(
        value = formState.batchName,
        onValueChange = { viewModel.onBatchNameChanged(it) },
        placeholder = { Text("e.g. Grade 10 Geometry") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(16.dp))

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f)) {
            Text("No. of Students", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = formState.numberOfStudents,
                onValueChange = { viewModel.onNumberOfStudentsChanged(it) },
                enabled = !viewModel.isEditMode, // roster count locked once created; managed separately later
                modifier = Modifier.fillMaxWidth()
            )
        }
        Column(Modifier.weight(1f)) {
            Text("Total Fee (৳)", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = formState.totalMoney,
                onValueChange = { viewModel.onTotalMoneyChanged(it) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    Spacer(Modifier.height(16.dp))

    Text("Weekly Schedule", fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    FlowRowDays(formState.selectedDays) { day -> viewModel.onDayToggled(day) }
    Spacer(Modifier.height(16.dp))

    Text("Class Time", fontWeight = FontWeight.Bold)
    OutlinedTextField(
        value = formState.time,
        onValueChange = { viewModel.onTimeChanged(it) },
        placeholder = { Text("e.g. 17:00") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun FlowRowDays(selectedDays: Set<String>, onToggle: (String) -> Unit) {
    Column {
        ALL_DAYS.chunked(4).forEach { rowDays ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowDays.forEach { day ->
                    FilterChip(
                        selected = selectedDays.contains(day),
                        onClick = { onToggle(day) },
                        label = { Text(day.take(3)) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Step2StudentRoster(formState: AddEditBatchFormState, viewModel: AddEditBatchViewModel) {
    Text("Student Roster", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(formState.studentEntries.size) { index ->
            val entry = formState.studentEntries[index]
            Column {
                Text("Student ${index + 1}", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = entry.name,
                    onValueChange = { viewModel.onStudentFieldChanged(index, entry.copy(name = it)) },
                    placeholder = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = entry.phone,
                    onValueChange = { viewModel.onStudentFieldChanged(index, entry.copy(phone = it)) },
                    placeholder = { Text("Phone number") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = entry.description,
                    onValueChange = { viewModel.onStudentFieldChanged(index, entry.copy(description = it)) },
                    placeholder = { Text("Short description (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}