package com.digifello.tuitionmanager.ui.finance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digifello.tuitionmanager.ui.common.UiState
import com.digifello.tuitionmanager.util.CurrencyFormatter

@Composable
fun FinanceScreen(
    onBatchClick: (String) -> Unit,
    viewModel: FinanceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Finance Overview") }) }) { padding ->
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
                val data = state.data
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(shape = RoundedCornerShape(16.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                Text("This Month", style = MaterialTheme.typography.labelLarge)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "${CurrencyFormatter.format(data.receivedThisMonth)} / ${CurrencyFormatter.format(data.expectedThisMonth)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    item {
                        Card(shape = RoundedCornerShape(16.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Pending Collection", fontWeight = FontWeight.Bold)
                                    Text(CurrencyFormatter.format(data.totalDue), color = MaterialTheme.colorScheme.error)
                                }
                                Spacer(Modifier.height(8.dp))
                                data.dueItems.forEach { due ->
                                    Row(
                                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${due.batchName} (${due.month})", style = MaterialTheme.typography.bodyMedium)
                                        Text(CurrencyFormatter.format(due.dueAmount), color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("Batch Breakdowns", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    items(data.batchBreakdown) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onBatchClick(item.batchId) },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(item.batchName, fontWeight = FontWeight.Bold)
                                    Text(item.status.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall)
                                }
                                Text(CurrencyFormatter.format(item.amountPaid))
                            }
                        }
                    }
                }
            }
        }
    }
}