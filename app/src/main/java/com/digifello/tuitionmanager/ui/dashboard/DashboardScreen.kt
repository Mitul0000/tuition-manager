package com.digifello.tuitionmanager.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digifello.tuitionmanager.ui.common.AppCard
import com.digifello.tuitionmanager.ui.common.PaymentStatus
import com.digifello.tuitionmanager.ui.common.PaymentStatusChip
import com.digifello.tuitionmanager.ui.common.UiState
import com.digifello.tuitionmanager.ui.common.WeekdayDotStrip
import com.digifello.tuitionmanager.ui.theme.ChalkWhite
import com.digifello.tuitionmanager.ui.theme.Ink40
import com.digifello.tuitionmanager.ui.theme.Ink60
import com.digifello.tuitionmanager.ui.theme.InkNavy
import com.digifello.tuitionmanager.ui.theme.Marigold
import com.digifello.tuitionmanager.ui.theme.NumberStyle
import com.digifello.tuitionmanager.ui.theme.PaidGreen
import com.digifello.tuitionmanager.ui.theme.PartialAmber
import com.digifello.tuitionmanager.ui.theme.UnpaidCrimson

// Full-day-name -> 1(Mon)..7(Sun), matching how Batch.days is stored ("Monday", etc.)
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

private fun statusAccentColor(status: PaymentStatus) = when (status) {
    PaymentStatus.PAID -> PaidGreen
    PaymentStatus.PARTIAL -> PartialAmber
    PaymentStatus.UNPAID -> UnpaidCrimson
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onBatchClick: (String) -> Unit,
    onAddBatchClick: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = ChalkWhite,
        topBar = {
            TopAppBar(
                title = { Text("TutorDesk", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ChalkWhite,
                    titleContentColor = InkNavy
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBatchClick,
                containerColor = Marigold,
                contentColor = InkNavy
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add batch")
            }
        }
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Marigold)
                }
            }
            is UiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(
                        "Something went wrong: ${state.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink60
                    )
                }
            }
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text(
                            "Tap + to create your first batch.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Ink60
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.data, key = { it.batch.id }) { batchWithStatus ->
                            val status = statusFrom(batchWithStatus.paymentStatus)
                            BatchCard(
                                name = batchWithStatus.batch.name,
                                dayIndices = dayNamesToIndices(batchWithStatus.batch.days),
                                time = batchWithStatus.batch.time,
                                studentCount = batchWithStatus.batch.numberOfStudents,
                                status = status,
                                onClick = { onBatchClick(batchWithStatus.batch.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BatchCard(
    name: String,
    dayIndices: Set<Int>,
    time: String,
    studentCount: Int,
    status: PaymentStatus,
    onClick: () -> Unit
) {
    AppCard(accentColor = statusAccentColor(status), onClick = onClick) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(name, style = MaterialTheme.typography.titleMedium, color = InkNavy)
                PaymentStatusChip(status)
            }
            Spacer(Modifier.height(10.dp))
            WeekdayDotStrip(activeDays = dayIndices)
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(time, style = NumberStyle.meta, color = Ink60)
                Text(
                    "$studentCount ${if (studentCount == 1) "Student" else "Students"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink40
                )
            }
        }
    }
}