package com.digifello.tutordesk.ui.Screens.Batchlistcontent

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.digifello.tutordesk.data.model.Batch
import com.digifello.tutordesk.ui.theme.CardWhite
import com.digifello.tutordesk.ui.theme.CardWhiteDark
import com.digifello.tutordesk.ui.theme.Ink_Navy
import com.digifello.tutordesk.ui.theme.Ink_NavyDark
import com.digifello.tutordesk.ui.theme.Marigold
import com.digifello.tutordesk.ui.theme.SlateBlue
import com.digifello.tutordesk.ui.theme.SlateBlueDark

@Composable
fun BatchListContent(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = HomeViewModel(),
    onBatchClick: (Batch) -> Unit = {}
) {
    val uiState by homeViewModel.uiState.collectAsState()

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
                    text = uiState.errorMessage ?: "Something went wrong",
                    color = textColor,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                )
            }

            uiState.batches.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No batches yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap + to create your first batch",
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryColor
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        TotalEarnedCard(
                            totalEarned = uiState.totalEarnedAllTime,
                            cardColor = cardColor,
                            textColor = textColor,
                            secondaryColor = secondaryColor
                        )
                    }

                    items(uiState.batches, key = { it.id }) { batch ->
                        BatchCard(
                            batch = batch,
                            cardColor = cardColor,
                            textColor = textColor,
                            secondaryColor = secondaryColor,
                            onClick = { onBatchClick(batch) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TotalEarnedCard(
    totalEarned: Long,
    cardColor: Color,
    textColor: Color,
    secondaryColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Total earned",
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
private fun BatchCard(
    batch: Batch,
    cardColor: Color,
    textColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = batch.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "₹${batch.totalMoney}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Marigold,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${batch.studentCount} students · ${batch.time}",
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryColor
            )

            if (batch.days.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = batch.days.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryColor
                )
            }
        }
    }
}