package com.digifello.tutordesk.ui.Screens.finance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digifello.tutordesk.ui.theme.CardWhite
import com.digifello.tutordesk.ui.theme.CardWhiteDark
import com.digifello.tutordesk.ui.theme.Ink_Navy
import com.digifello.tutordesk.ui.theme.Ink_NavyDark
import com.digifello.tutordesk.ui.theme.Marigold
import com.digifello.tutordesk.ui.theme.SlateBlue
import com.digifello.tutordesk.ui.theme.SlateBlueDark

@Composable
fun FinanceScreen(
    modifier: Modifier = Modifier,
    viewModel: FinanceViewModel = viewModel(),
    onBatchClick: (com.digifello.tutordesk.data.model.Batch) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    val isDark = isSystemInDarkTheme()
    val cardColor = if (isDark) CardWhiteDark else CardWhite
    val textColor = if (isDark) Ink_NavyDark else Ink_Navy
    val secondaryColor = if (isDark) SlateBlueDark else SlateBlue

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Marigold
                )
            }

            uiState.errorMessage != null -> {
                Text(
                    text = uiState.errorMessage ?: "",
                    color = textColor,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        ThisMonthSummaryCard(
                            uiState = uiState,
                            cardColor = cardColor,
                            textColor = textColor,
                            secondaryColor = secondaryColor
                        )
                    }

                    item {
                        AllTimeCard(
                            totalEarned = uiState.totalEarnedAllTime,
                            cardColor = cardColor,
                            textColor = textColor,
                            secondaryColor = secondaryColor
                        )
                    }

                    item {
                        Text(
                            text = "Batch breakdown — ${uiState.currentMonth}",
                            style = MaterialTheme.typography.titleMedium,
                            color = textColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (uiState.batchRows.isEmpty()) {
                        item {
                            Text(
                                text = "No batches yet",
                                color = secondaryColor,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    items(uiState.batchRows, key = { it.batch.id }) { row ->
                        BatchFinanceCard(
                            row = row,
                            cardColor = cardColor,
                            textColor = textColor,
                            secondaryColor = secondaryColor,
                            onClick = { onBatchClick(row.batch) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThisMonthSummaryCard(
    uiState: FinanceUiState,
    cardColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    secondaryColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "This month",
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "₹${uiState.collectedThisMonth} of ₹${uiState.expectedThisMonth} collected",
                style = MaterialTheme.typography.headlineSmall,
                color = textColor,
                fontWeight = FontWeight.Bold
            )

            LinearProgressIndicator(
                progress = { uiState.collectionProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Marigold,
                trackColor = secondaryColor.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Collected", style = MaterialTheme.typography.labelMedium, color = secondaryColor)
                    Text(
                        "₹${uiState.collectedThisMonth}",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column {
                    Text("Pending", style = MaterialTheme.typography.labelMedium, color = secondaryColor)
                    Text(
                        "₹${uiState.pendingThisMonth}",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun AllTimeCard(
    totalEarned: Long,
    cardColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    secondaryColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Total earned till now",
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "₹$totalEarned",
                style = MaterialTheme.typography.headlineMedium,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BatchFinanceCard(
    row: BatchFinanceRow,
    cardColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    secondaryColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    val progress = if (row.expected > 0) (row.collected.toFloat() / row.expected).coerceIn(0f, 1f) else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = row.batch.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = textColor,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "₹${row.collected} / ₹${row.expected}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Marigold,
                trackColor = secondaryColor.copy(alpha = 0.2f)
            )
        }
    }
}