package com.digifello.tuitionmanager.data.model

import com.google.firebase.Timestamp

/**
 * Represents one tuition batch (e.g. "Class 10 - Advanced Algebra").
 * Maps directly to a document in the "batches" Firestore collection.
 */
data class Batch(
    val id: String = "",
    val name: String = "",
    val days: List<String> = emptyList(),
    val time: String = "",
    val numberOfStudents: Int = 0,
    val totalMoney: Double = 0.0,
    val createdAt: Timestamp = Timestamp.now()
)