package com.digifello.tutordesk.ui.Screens.routine

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.digifello.tutordesk.data.model.Batch
import com.digifello.tutordesk.ui.theme.CardWhite
import com.digifello.tutordesk.ui.theme.CardWhiteDark
import com.digifello.tutordesk.ui.theme.Ink_Navy
import com.digifello.tutordesk.ui.theme.Ink_NavyDark
import com.digifello.tutordesk.ui.theme.Marigold
import com.digifello.tutordesk.ui.theme.SlateBlue
import com.digifello.tutordesk.ui.theme.SlateBlueDark
import androidx.compose.foundation.clickable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RoutineScreen(
    modifier: Modifier = Modifier,
    routineViewModel: RoutineViewModel = viewModel(),
    onBatchClick: (Batch) -> Unit = {}
) {
    val uiState by routineViewModel.uiState.collectAsState()

    val isDark = isSystemInDarkTheme()
    val cardColor = if (isDark) CardWhiteDark else CardWhite
    val textColor = if (isDark) Ink_NavyDark else Ink_Navy
    val secondaryColor = if (isDark) SlateBlueDark else SlateBlue

    Column(modifier = modifier.fillMaxSize()) {

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(WEEKDAYS) { day ->
                val isSelected = day == uiState.selectedDay
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) Marigold else cardColor)
                        .clickable { routineViewModel.selectDay(day) }
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        color = if (isSelected) Ink_Navy else secondaryColor,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        val batchesForDay = uiState.batchesByDay[uiState.selectedDay] ?: emptyList()

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Marigold)
                }
            }

            uiState.errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.errorMessage ?: "", color = textColor)
                }
            }

            batchesForDay.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No classes on ${uiState.selectedDay}",
                            style = MaterialTheme.typography.titleMedium,
                            color = textColor,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enjoy the free day",
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondaryColor
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(batchesForDay, key = { it.id }) { batch ->
                        RoutineBatchCard(
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
private fun RoutineBatchCard(
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Marigold)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = batch.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${batch.studentCount} students",
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryColor
                )
            }
            Text(
                text = batch.time,
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}