package com.digifello.tuitionmanager.ui.batchdetail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.digifello.tuitionmanager.ui.common.AppCard
import com.digifello.tuitionmanager.ui.common.AppDestructiveButton
import com.digifello.tuitionmanager.ui.common.AppPrimaryButton
import com.digifello.tuitionmanager.ui.common.AppSecondaryButton
import com.digifello.tuitionmanager.ui.common.AppTextField
import com.digifello.tuitionmanager.ui.common.PaymentStatus
import com.digifello.tuitionmanager.ui.common.PaymentStatusChip
import com.digifello.tuitionmanager.ui.common.WeekdayDotStrip
import com.digifello.tuitionmanager.ui.theme.ChalkWhite
import com.digifello.tuitionmanager.ui.theme.Ink40
import com.digifello.tuitionmanager.ui.theme.Ink60
import com.digifello.tuitionmanager.ui.theme.InkNavy
import com.digifello.tuitionmanager.ui.theme.Marigold
import com.digifello.tuitionmanager.ui.theme.NumberStyle
import com.digifello.tuitionmanager.ui.theme.PaidGreen
import com.digifello.tuitionmanager.ui.theme.UnpaidCrimson
import com.digifello.tuitionmanager.util.CurrencyFormatter

private val DAY_INDEX = mapOf(
    "Monday" to 1, "Tuesday" to 2, "Wednesday" to 3, "Thursday" to 4,
    "Friday" to 5, "Saturday" to 6, "Sunday" to 7
)
private fun dayNamesToIndices(days: List<String>): Set<Int> =
    days.mapNotNull { DAY_INDEX[it] }.toSet()

private fun statusFrom(raw: String): PaymentStatus = when (raw) {
    "paid" -> PaymentStatus.PAID
    "partial" -> PaymentStatus.PARTIAL
    else -> PaymentStatus.UNPAID
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchDetailScreen(
    viewModel: BatchDetailViewModel,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPaymentSheet by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = ChalkWhite,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        uiState.batch?.name ?: "",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = InkNavy)
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit batch", tint = InkNavy)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete batch", tint = UnpaidCrimson)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ChalkWhite,
                    titleContentColor = InkNavy
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Marigold)
            }
            return@Scaffold
        }

        val batch = uiState.batch ?: return@Scaffold
        val payment = uiState.currentPayment
        val status = statusFrom(payment?.status ?: "pending")

        LazyColumnContent(
            batch = batch,
            students = uiState.students,
            payment = payment,
            status = status,
            onMarkPaymentClick = { showPaymentSheet = true }
        )

        if (showPaymentSheet) {
            MarkPaymentSheet(
                expectedAmount = payment?.expectedAmount ?: batch.totalMoney,
                onConfirm = { amount ->
                    viewModel.markPayment(amount)
                    showPaymentSheet = false
                },
                onDismiss = { showPaymentSheet = false }
            )
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Delete this batch?", color = InkNavy) },
                text = {
                    Text(
                        "This will permanently remove the batch, its students, and payment history.",
                        color = Ink60
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.deleteBatch(onDeleted) }) {
                        Text("Delete", color = UnpaidCrimson)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Cancel", color = Ink60)
                    }
                },
                containerColor = ChalkWhite
            )
        }
    }
}

@Composable
private fun LazyColumnContent(
    batch: com.digifello.tuitionmanager.data.model.Batch,
    students: List<com.digifello.tuitionmanager.data.model.Student>,
    payment: com.digifello.tuitionmanager.data.model.Payment?,
    status: PaymentStatus,
    onMarkPaymentClick: () -> Unit
) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WeekdayDotStrip(activeDays = dayNamesToIndices(batch.days), showLabels = true)
                Spacer(Modifier.width(12.dp))
                Text(batch.time, style = NumberStyle.meta, color = Ink60)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "${batch.numberOfStudents} Students Enrolled",
                style = MaterialTheme.typography.bodySmall,
                color = Ink40
            )
        }

        item {
            val expected = payment?.expectedAmount ?: batch.totalMoney
            val paid = payment?.amountPaid ?: 0.0
            AppCard {
                Column(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Financial Health", style = MaterialTheme.typography.titleSmall, color = InkNavy)
                        PaymentStatusChip(status)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${CurrencyFormatter.format(paid)} / ${CurrencyFormatter.format(expected)}",
                        style = NumberStyle.amountLarge,
                        color = InkNavy
                    )
                    LinearProgressIndicator(
                        progress = if (expected > 0) (paid / expected).toFloat().coerceIn(0f, 1f) else 0f,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        color = PaidGreen,
                        trackColor = Ink40.copy(alpha = 0.15f)
                    )
                    Text(
                        "Remaining: ${CurrencyFormatter.format((expected - paid).coerceAtLeast(0.0))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink60
                    )
                }
            }
        }

        item {
            AppPrimaryButton(text = "Mark New Payment", onClick = onMarkPaymentClick)
        }

        item {
            Text("Batch Roster", style = MaterialTheme.typography.titleMedium, color = InkNavy)
        }

        items(students, key = { it.id }) { student ->
            AppCard {
                Column(Modifier.fillMaxWidth()) {
                    Text(student.name, style = MaterialTheme.typography.titleSmall, color = InkNavy)
                    Spacer(Modifier.height(2.dp))
                    Text(student.phone, style = NumberStyle.meta, color = Ink60)
                    student.description?.let {
                        Spacer(Modifier.height(4.dp))
                        Text("\"$it\"", style = MaterialTheme.typography.bodySmall, color = Ink40)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarkPaymentSheet(
    expectedAmount: Double,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ChalkWhite
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text("Mark Payment", style = MaterialTheme.typography.titleLarge, color = InkNavy)
            Spacer(Modifier.height(4.dp))
            Text(
                "Expected: ${CurrencyFormatter.format(expectedAmount)}",
                style = MaterialTheme.typography.bodyMedium,
                color = Ink60
            )
            Spacer(Modifier.height(16.dp))
            AppTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = "Amount received (৳)",
                keyboardType = KeyboardType.Number
            )
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppSecondaryButton(text = "Cancel", onClick = onDismiss, modifier = Modifier.weight(1f))
                AppPrimaryButton(
                    text = "Confirm",
                    onClick = { amountText.toDoubleOrNull()?.let { onConfirm(it) } },
                    enabled = amountText.toDoubleOrNull() != null,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}