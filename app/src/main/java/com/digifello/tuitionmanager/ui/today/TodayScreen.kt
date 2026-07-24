package com.digifello.tuitionmanager.ui.today

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
import com.digifello.tuitionmanager.ui.common.UiState
import com.digifello.tuitionmanager.ui.theme.ChalkWhite
import com.digifello.tuitionmanager.ui.theme.Ink40
import com.digifello.tuitionmanager.ui.theme.Ink60
import com.digifello.tuitionmanager.ui.theme.InkNavy
import com.digifello.tuitionmanager.ui.theme.Marigold
import com.digifello.tuitionmanager.ui.theme.NumberStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onBatchClick: (String) -> Unit,
    viewModel: TodayViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = ChalkWhite,
        topBar = {
            TopAppBar(
                title = { Text("Routine", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ChalkWhite,
                    titleContentColor = InkNavy
                )
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(viewModel.todayName, style = MaterialTheme.typography.titleLarge, color = InkNavy)
                Text(viewModel.todayDateFormatted, style = MaterialTheme.typography.bodyMedium, color = Ink60)
            }

            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Marigold)
                    }
                }
                is UiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Something went wrong: ${state.message}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Ink60
                        )
                    }
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "No classes scheduled today.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Ink60
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.data, key = { it.id }) { batch ->
                                AppCard(onClick = { onBatchClick(batch.id) }) {
                                    Column(Modifier.fillMaxWidth()) {
                                        Text(batch.time, style = NumberStyle.meta, color = Ink60)
                                        Spacer(Modifier.height(4.dp))
                                        Text(batch.name, style = MaterialTheme.typography.titleMedium, color = InkNavy)
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "${batch.numberOfStudents} Students Enrolled",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Ink40
                                        )
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