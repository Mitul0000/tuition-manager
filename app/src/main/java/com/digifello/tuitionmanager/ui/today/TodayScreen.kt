package com.digifello.tuitionmanager.ui.today

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
import com.digifello.tuitionmanager.ui.dashboard.StatusBadge

@Composable
fun TodayScreen(
    onBatchClick: (String) -> Unit,
    viewModel: TodayViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Today") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.padding(16.dp)) {
                Text(viewModel.todayName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(viewModel.todayDateFormatted, style = MaterialTheme.typography.bodyMedium)
            }

            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Something went wrong: ${state.message}")
                    }
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No classes scheduled today.")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.data) { batch ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { onBatchClick(batch.id) },
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(batch.time, style = MaterialTheme.typography.labelMedium)
                                        Spacer(Modifier.height(4.dp))
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(batch.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text("${batch.numberOfStudents} Students Enrolled", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}