package com.digifello.tuitionmanager.ui.batchdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.digifello.tuitionmanager.ui.dashboard.StatusBadge
import com.digifello.tuitionmanager.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchDetailScreen(
    viewModel: BatchDetailViewModel,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.batch?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit batch")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete batch")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val batch = uiState.batch ?: return@Scaffold
        val payment = uiState.currentPayment

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("${batch.days.joinToString(" & ")} • ${batch.time}")
                Text("${batch.numberOfStudents} Students Enrolled")
            }

            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Financial Health", fontWeight = FontWeight.Bold)
                            StatusBadge(payment?.status ?: "pending")
                        }
                        Spacer(Modifier.height(8.dp))
                        val expected = payment?.expectedAmount ?: batch.totalMoney
                        val paid = payment?.amountPaid ?: 0.0
                        Text(
                            "${CurrencyFormatter.format(paid)} / ${CurrencyFormatter.format(expected)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        LinearProgressIndicator(
                            progress = if (expected > 0) (paid / expected).toFloat().coerceIn(0f, 1f) else 0f,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                        Text("Remaining: ${CurrencyFormatter.format((expected - paid).coerceAtLeast(0.0))}")
                    }
                }
            }

            item {
                Button(onClick = { showPaymentDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Mark New Payment")
                }
            }

            item {
                Text("Batch Roster", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }

            items(uiState.students) { student ->
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(student.name, fontWeight = FontWeight.Bold)
                        Text(student.phone, style = MaterialTheme.typography.bodySmall)
                        student.description?.let {
                            Spacer(Modifier.height(4.dp))
                            Text("\"$it\"", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        if (showPaymentDialog) {
            MarkPaymentDialog(
                expectedAmount = payment?.expectedAmount ?: batch.totalMoney,
                onConfirm = { amount ->
                    viewModel.markPayment(amount)
                    showPaymentDialog = false
                },
                onDismiss = { showPaymentDialog = false }
            )
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Delete this batch?") },
                text = { Text("This will permanently remove the batch, its students, and payment history.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.deleteBatch(onDeleted) }) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun MarkPaymentDialog(
    expectedAmount: Double,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mark Payment") },
        text = {
            Column {
                Text("Expected: ${CurrencyFormatter.format(expectedAmount)}")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount received") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                amountText.toDoubleOrNull()?.let { onConfirm(it) }
            }) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}