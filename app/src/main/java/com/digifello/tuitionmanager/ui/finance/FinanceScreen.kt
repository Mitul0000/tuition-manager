package com.digifello.tuitionmanager.ui.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.digifello.tuitionmanager.ui.theme.ChalkWhite
import com.digifello.tuitionmanager.ui.theme.Ink60
import com.digifello.tuitionmanager.ui.theme.InkNavy
import com.digifello.tuitionmanager.ui.theme.Marigold
import com.digifello.tuitionmanager.ui.theme.NumberStyle
import com.digifello.tuitionmanager.ui.theme.PaidGreen
import com.digifello.tuitionmanager.ui.theme.PartialAmber
import com.digifello.tuitionmanager.ui.theme.UnpaidCrimson
import com.digifello.tuitionmanager.ui.theme.UnpaidCrimsonBg
import com.digifello.tuitionmanager.util.CurrencyFormatter

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
fun FinanceScreen(
    onBatchClick: (String) -> Unit,
    viewModel: FinanceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = ChalkWhite,
        topBar = {
            TopAppBar(
                title = { Text("Finance", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ChalkWhite,
                    titleContentColor = InkNavy
                )
            )
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
                val data = state.data
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        AppCard {
                            Column(Modifier.fillMaxWidth()) {
                                Text("This Month", style = MaterialTheme.typography.labelLarge, color = Ink60)
                                Spacer(Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(CurrencyFormatter.format(data.receivedThisMonth), style = NumberStyle.amountLarge, color = InkNavy)
                                    Text(
                                        " / ${CurrencyFormatter.format(data.expectedThisMonth)}",
                                        style = NumberStyle.meta,
                                        color = Ink60,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        AppCard(accentColor = UnpaidCrimson) {
                            Column(Modifier.fillMaxWidth()) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Pending Collection", style = MaterialTheme.typography.titleSmall, color = InkNavy)
                                    Text(CurrencyFormatter.format(data.totalDue), style = NumberStyle.amount, color = UnpaidCrimson)
                                }
                                if (data.dueItems.isNotEmpty()) {
                                    Spacer(Modifier.height(10.dp))
                                    data.dueItems.forEach { due ->
                                        Row(
                                            Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                "${due.batchName} (${due.month})",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Ink60
                                            )
                                            Text(CurrencyFormatter.format(due.dueAmount), style = NumberStyle.meta, color = UnpaidCrimson)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("Batch Breakdown", style = MaterialTheme.typography.titleMedium, color = InkNavy)
                    }

                    items(data.batchBreakdown, key = { it.batchId }) { item ->
                        val status = statusFrom(item.status)
                        AppCard(accentColor = statusAccentColor(status), onClick = { onBatchClick(item.batchId) }) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(item.batchName, style = MaterialTheme.typography.titleSmall, color = InkNavy)
                                    Spacer(Modifier.height(4.dp))
                                    PaymentStatusChip(status)
                                }
                                Text(CurrencyFormatter.format(item.amountPaid), style = NumberStyle.amount, color = InkNavy)
                            }
                        }
                    }
                }
            }
        }
    }
}