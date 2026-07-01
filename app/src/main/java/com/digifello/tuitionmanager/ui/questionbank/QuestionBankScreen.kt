package com.digifello.tuitionmanager.ui.questionbank

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Composable
fun QuestionBankScreen() {
    Scaffold(topBar = { TopAppBar(title = { Text("Question Bank") }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Coming Soon", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text("Question paper creation is on the way.")
            }
        }
    }
}