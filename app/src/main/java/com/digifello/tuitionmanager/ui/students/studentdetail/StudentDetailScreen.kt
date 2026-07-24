package com.digifello.tuitionmanager.ui.students.studentdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.digifello.tuitionmanager.ui.common.AppCard
import com.digifello.tuitionmanager.ui.common.AppTextField
import com.digifello.tuitionmanager.ui.common.AppPrimaryButton
import com.digifello.tuitionmanager.ui.common.PaymentStatus
import com.digifello.tuitionmanager.ui.common.PaymentStatusChip
import com.digifello.tuitionmanager.ui.common.StudentDetailViewModelFactory
import com.digifello.tuitionmanager.ui.common.UiState
import com.digifello.tuitionmanager.ui.common.computePaymentStatus
import com.digifello.tuitionmanager.ui.theme.ChalkWhite
import com.digifello.tuitionmanager.ui.theme.Ink40
import com.digifello.tuitionmanager.ui.theme.Ink60
import com.digifello.tuitionmanager.ui.theme.InkNavy
import com.digifello.tuitionmanager.ui.theme.Marigold
import com.digifello.tuitionmanager.ui.theme.SkyTint
import com.digifello.tuitionmanager.ui.theme.TuitionManagerTheme
import com.digifello.tuitionmanager.ui.theme.UnpaidCrimson
import com.digifello.tuitionmanager.util.CurrencyFormatter

@Composable
fun StudentDetailScreen(
    studentId: String,
    onBackClick: () -> Unit,
    viewModel: StudentDetailViewModel = viewModel(
        key = studentId,
        factory = StudentDetailViewModelFactory(studentId)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChalkWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = InkNavy)
            }
            Text(
                text = "Student",
                style = MaterialTheme.typography.titleLarge,
                color = InkNavy,
                modifier = Modifier.weight(1f)
            )
            when (val state = uiState) {
                is UiState.Success -> {
                    IconButton(onClick = { isEditing = !isEditing }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = InkNavy)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = UnpaidCrimson)
                    }
                }
                else -> {}
            }
        }

        when (val state = uiState) {
            is UiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Marigold)
                }
            }
            is UiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = Ink60, style = MaterialTheme.typography.bodyMedium)
                }
            }
            is UiState.Success -> {
                val student = state.data.student
                if (student == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Student not found", color = Ink60)
                    }
                    return@Column
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(SkyTint),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                tint = Marigold,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        if (!isEditing) {
                            Text(
                                text = student.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = InkNavy,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = student.batchName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Marigold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isEditing) {
                        EditStudentForm(
                            initialName = student.name,
                            initialPhone = student.phone,
                            initialDescription = student.description.orEmpty(),
                            onSave = { name, phone, description ->
                                viewModel.updateStudent(name, phone, description.ifBlank { null }) {
                                    isEditing = false
                                }
                            },
                            onCancel = { isEditing = false }
                        )
                    } else {
                        AppCard(modifier = Modifier.padding(bottom = 16.dp)) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Call, contentDescription = null, tint = Ink40, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = student.phone.ifBlank { "No phone number" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = InkNavy
                                    )
                                }
                                if (!student.description.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = student.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Ink60
                                    )
                                }
                            }
                        }

                        val payment = state.data.currentPayment
                        val paymentStatus = computePaymentStatus(
                            payment?.amountPaid ?: 0.0,
                            payment?.expectedAmount ?: 0.0
                        )

                        Text(
                            text = "THIS MONTH'S BATCH PAYMENT",
                            style = MaterialTheme.typography.labelMedium,
                            color = Ink40,
                            modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
                        )
                        AppCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${CurrencyFormatter.format(payment?.amountPaid ?: 0.0)} / ${CurrencyFormatter.format(payment?.expectedAmount ?: 0.0)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = InkNavy
                                    )
                                    Text(
                                        text = "Reflects the whole batch, not just this student",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Ink60
                                    )
                                }
                                PaymentStatusChip(status = paymentStatus)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Remove this student?", style = MaterialTheme.typography.titleLarge, color = InkNavy) },
            text = {
                Text(
                    "This removes them from their batch roster. This can't be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink60
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.deleteStudent(onDeleted = onBackClick)
                }) {
                    Text("Remove", color = UnpaidCrimson, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = Ink60)
                }
            }
        )
    }
}

@Composable
private fun EditStudentForm(
    initialName: String,
    initialPhone: String,
    initialDescription: String,
    onSave: (name: String, phone: String, description: String) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var description by remember { mutableStateOf(initialDescription) }

    Column(modifier = Modifier.fillMaxWidth()) {
        AppTextField(value = name, onValueChange = { name = it }, label = "Name")
        Spacer(modifier = Modifier.height(12.dp))
        AppTextField(value = phone, onValueChange = { phone = it }, label = "Phone")
        Spacer(modifier = Modifier.height(12.dp))
        AppTextField(value = description, onValueChange = { description = it }, label = "Notes (optional)")
        Spacer(modifier = Modifier.height(20.dp))
        AppPrimaryButton(text = "Save Changes", onClick = { onSave(name, phone, description) })
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onCancel) {
            Text("Cancel", color = Ink60)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StudentDetailScreenPreview() {
    TuitionManagerTheme {
        StudentDetailScreen(studentId = "preview", onBackClick = {})
    }
}