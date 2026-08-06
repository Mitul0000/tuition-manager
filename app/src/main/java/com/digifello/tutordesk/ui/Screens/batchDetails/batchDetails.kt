package com.digifello.tutordesk.ui.Screens.batchDetails

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digifello.tutordesk.data.model.Student
import com.digifello.tutordesk.ui.Screens.routine.WEEKDAYS
import com.digifello.tutordesk.ui.theme.CardWhite
import com.digifello.tutordesk.ui.theme.CardWhiteDark
import com.digifello.tutordesk.ui.theme.ChalkWhite
import com.digifello.tutordesk.ui.theme.ChalkWhiteDark
import com.digifello.tutordesk.ui.theme.Ink_Navy
import com.digifello.tutordesk.ui.theme.Ink_NavyDark
import com.digifello.tutordesk.ui.theme.Marigold
import com.digifello.tutordesk.ui.theme.SlateBlue
import com.digifello.tutordesk.ui.theme.SlateBlueDark

class BatchDetailsViewModelFactory(private val batchId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return BatchDetailsViewModel(batchId = batchId) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchDetailsScreen(
    batchId: String,
    onBack: () -> Unit,
    viewModel: BatchDetailsViewModel = viewModel(factory = BatchDetailsViewModelFactory(batchId))
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler(enabled = uiState.isEditingBatch) {
        viewModel.cancelEditingBatch()
    }

    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) ChalkWhiteDark else ChalkWhite
    val cardColor = if (isDark) CardWhiteDark else CardWhite
    val textColor = if (isDark) Ink_NavyDark else Ink_Navy
    val secondaryColor = if (isDark) SlateBlueDark else SlateBlue

    var showDeleteBatchConfirm by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var studentPendingDelete by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf<Student?>(null)
    }

    LaunchedEffect(uiState.isBatchDeleted) {
        if (uiState.isBatchDeleted) onBack()
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        uiState.batch?.name ?: "Batch",
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                actions = {
                    if (!uiState.isEditingBatch) {
                        IconButton(onClick = { viewModel.startEditingBatch() }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit batch", tint = textColor)
                        }
                        IconButton(onClick = { showDeleteBatchConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete batch", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        floatingActionButton = {
            if (!uiState.isEditingBatch) {
                FloatingActionButton(
                    onClick = { viewModel.startAddingStudent() },
                    containerColor = Marigold,
                    contentColor = Ink_Navy
                ) {
                    Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Add student")
                }
            }
        }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Marigold
                    )
                }

                uiState.isBatchDeleted -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Marigold
                    )
                }

                uiState.batch == null -> {
                    Text(
                        text = "Batch not found",
                        color = textColor,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.isEditingBatch -> {
                    EditBatchForm(
                        uiState = uiState,
                        viewModel = viewModel,
                        cardColor = cardColor,
                        textColor = textColor,
                        secondaryColor = secondaryColor
                    )
                }

                else -> {
                    BatchDetailsContent(
                        uiState = uiState,
                        cardColor = cardColor,
                        textColor = textColor,
                        secondaryColor = secondaryColor,
                        onDeleteStudent = { studentPendingDelete = it },
                        onMarkBatchPaidClick = { viewModel.requestMarkBatchPaid() },
                        onStudentPaymentClick = { viewModel.openPaymentDialog(it) }
                    )
                }
            }
        }
    }


    if (uiState.isAddingStudent) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelAddingStudent() },
            title = { Text("Add Student") },
            text = {
                Column {
                    OutlinedTextField(
                        value = uiState.newStudentName,
                        onValueChange = viewModel::onNewStudentNameChange,
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.newStudentPhone,
                        onValueChange = viewModel::onNewStudentPhoneChange,
                        label = { Text("Phone (optional)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(uiState.errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.addStudent() }, enabled = !uiState.isSaving) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelAddingStudent() }) { Text("Cancel") }
            }
        )
    }


    if (showDeleteBatchConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteBatchConfirm = false },
            title = { Text("Delete this batch?") },
            text = { Text("Students in this batch will be unassigned, not deleted. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteBatchConfirm = false
                    viewModel.deleteBatch()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteBatchConfirm = false }) { Text("Cancel") }
            }
        )
    }


    studentPendingDelete?.let { student ->
        AlertDialog(
            onDismissRequest = { studentPendingDelete = null },
            title = { Text("Remove ${student.name}?") },
            text = { Text("This will permanently remove them from the batch.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteStudent(student)
                    studentPendingDelete = null
                }) { Text("Remove", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { studentPendingDelete = null }) { Text("Cancel") }
            }
        )
    }

    if (uiState.showMarkBatchPaidConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelMarkBatchPaid() },
            title = { Text("Mark all students paid?") },
            text = { Text("Every student's expected fee for this month will be marked as fully paid. Students without a fee set for this month yet will be skipped.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmMarkBatchPaid() }, enabled = !uiState.isSaving) {
                    Text("Mark all paid")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelMarkBatchPaid() }) { Text("Cancel") }
            }
        )
    }

    uiState.studentForPaymentDialog?.let { student ->
        val payment = uiState.paymentsByStudentId[student.id]
        AlertDialog(
            onDismissRequest = { viewModel.closePaymentDialog() },
            title = { Text("${student.name} — ${uiState.currentMonth}") },
            text = {
                Column {
                    Text(
                        text = "Expected: ₹${payment?.expectedAmount ?: 0}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.paymentAmountText,
                        onValueChange = viewModel::onPaymentAmountChange,
                        label = { Text("Amount paid (₹)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.submitStudentPayment() }, enabled = !uiState.isSaving) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closePaymentDialog() }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun BatchDetailsContent(
    uiState: BatchDetailsUiState,
    cardColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    secondaryColor: androidx.compose.ui.graphics.Color,
    onDeleteStudent: (Student) -> Unit,
    onMarkBatchPaidClick: () -> Unit,
    onStudentPaymentClick: (Student) -> Unit
) {
    val batch = uiState.batch ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "₹${batch.totalMoney} total / month",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${batch.time} · ${batch.days.joinToString(", ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${uiState.students.size} students",
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryColor
                    )

                    if (uiState.students.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onMarkBatchPaidClick,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSaving
                        ) {
                            Text("Mark all paid for this month")
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Students",
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (uiState.students.isEmpty()) {
            item {
                Text(
                    text = "No students yet. Tap + to add one.",
                    color = secondaryColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        items(uiState.students, key = { it.id }) { student ->
            val payment = uiState.paymentsByStudentId[student.id]
            val status = payment?.status ?: "pending"
            val statusColor = when (status) {
                "paid" -> androidx.compose.ui.graphics.Color(0xFF2E7D32)
                "partial" -> androidx.compose.ui.graphics.Color(0xFFF9A825)
                else -> androidx.compose.ui.graphics.Color(0xFFC62828)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStudentPaymentClick(student) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = textColor,
                            fontWeight = FontWeight.Medium
                        )
                        if (student.phone.isNotBlank()) {
                            Text(
                                text = student.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = secondaryColor
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "₹${payment?.amountPaid ?: 0} / ₹${payment?.expectedAmount ?: 0} · $status",
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    IconButton(onClick = { onDeleteStudent(student) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove ${student.name}",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(64.dp)) } // room for FAB
    }
}

@Composable
private fun EditBatchForm(
    uiState: BatchDetailsUiState,
    viewModel: BatchDetailsViewModel,
    cardColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    secondaryColor: androidx.compose.ui.graphics.Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = uiState.editName,
            onValueChange = viewModel::onEditNameChange,
            label = { Text("Batch name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Column {
            Text("Class days", style = MaterialTheme.typography.labelLarge, color = secondaryColor)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WEEKDAYS.forEach { day ->
                    val isSelected = uiState.editDays.contains(day)
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) Marigold else cardColor)
                            .clickable { viewModel.toggleEditDay(day) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            color = if (isSelected) Ink_Navy else secondaryColor,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = uiState.editTime,
            onValueChange = viewModel::onEditTimeChange,
            label = { Text("Class time") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.editTotalMoney,
            onValueChange = viewModel::onEditTotalMoneyChange,
            label = { Text("Total monthly fee (₹)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )


        if (uiState.updateErrorMessage != null) {
            Text(uiState.updateErrorMessage, color = MaterialTheme.colorScheme.error)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { viewModel.cancelEditingBatch() },
                modifier = Modifier.weight(1f)
            ) { Text("Cancel") }

            Button(
                onClick = { viewModel.saveBatchEdits() },
                enabled = !uiState.isSaving,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Marigold, contentColor = Ink_Navy)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Ink_Navy, strokeWidth = 2.dp)
                } else {
                    Text("Save")
                }
            }
        }
    }
}