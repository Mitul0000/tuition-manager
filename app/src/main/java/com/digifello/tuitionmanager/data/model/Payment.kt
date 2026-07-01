package com.digifello.tuitionmanager.data.model

import com.google.firebase.Timestamp

/**
 * Represents one month's payment record for a batch.
 * Stored at: batches/{batchId}/payments/{month}
 * where {month} is a string like "2026-07" (also used as the document ID).
 */
data class Payment(
    val month: String = "",              // e.g. "2026-07" — also matches the document ID
    val status: String = "pending",      // one of: "pending", "partial", "paid"
    val expectedAmount: Double = 0.0,    // snapshot of Batch.totalMoney at record creation
    val amountPaid: Double = 0.0,        // starts at 0, updated via "Mark Payment"
    val paidOn: Timestamp? = null,        // set/updated whenever amountPaid changes
    val batchId: String = "",
    val batchName: String = ""
)
