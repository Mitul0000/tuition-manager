package com.digifello.tuitionmanager.data.model

import com.google.firebase.Timestamp

/**
 * Represents one tuition batch (e.g. "Class 10 - Advanced Algebra").
 * Maps directly to a document in the "batches" Firestore collection.
 */
data class Batch(
    val id: String = "",              // Firestore document ID, filled in after fetch
    val name: String = "",            // e.g. "Class 10 - Advanced Algebra"
    val days: List<String> = emptyList(), // e.g. ["Monday", "Wednesday", "Friday"]
    val time: String = "",            // stored as simple "HH:mm" string, e.g. "17:00"
    val numberOfStudents: Int = 0,
    val totalMoney: Double = 0.0,     // FIXED total fee for the whole batch (confirmed earlier)
    val createdAt: Timestamp = Timestamp.now()
)