package com.digifello.tuitionmanager.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digifello.tuitionmanager.ui.common.UiState

@Composable
fun DashboardScreen(
    onBatchClick: (String) -> Unit,
    onAddBatchClick: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("TutorFlow") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBatchClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add batch")
            }
        }
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Something went wrong: ${state.message}")
                }
            }
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text("No batches yet. Tap + to create your first one.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.data) { batchWithStatus ->
                            BatchCard(
                                name = batchWithStatus.batch.name,
                                days = batchWithStatus.batch.days.joinToString(", "),
                                time = batchWithStatus.batch.time,
                                studentCount = batchWithStatus.batch.numberOfStudents,
                                status = batchWithStatus.paymentStatus,
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
    days: String,
    time: String,
    studentCount: Int,
    status: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                StatusBadge(status)
            }
            Spacer(Modifier.height(8.dp))
            Text(days, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(4.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(time, style = MaterialTheme.typography.bodyMedium)
                Text("$studentCount Students", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (label, color) = when (status) {
        "paid" -> "Fully Paid" to MaterialTheme.colorScheme.tertiary
        "partial" -> "Partial Paid" to MaterialTheme.colorScheme.secondary
        else -> "Pending Dues" to MaterialTheme.colorScheme.error
    }
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(50)) {
        Text(
            label,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}