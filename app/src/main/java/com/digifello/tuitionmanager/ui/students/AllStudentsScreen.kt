package com.digifello.tuitionmanager.ui.students

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digifello.tuitionmanager.ui.common.UiState

@Composable
fun AllStudentsScreen(viewModel: AllStudentsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Students") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search by name or phone...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                singleLine = true
            )

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
                            Text("No students found.")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(state.data) { student ->
                                ListItem(
                                    headlineContent = { Text(student.name, fontWeight = FontWeight.Bold) },
                                    supportingContent = { Text(student.phone) },
                                    trailingContent = {
                                        AssistChip(onClick = {}, label = { Text(student.batchName) })
                                    }
                                )
                                Divider()
                            }
                        }
                    }
                }
            }
        }
    }
}