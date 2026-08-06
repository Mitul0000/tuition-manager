package com.digifello.tutordesk.data.model

import com.google.firebase.Timestamp

data class Payment(
    val month: String = "",
    val status: String = "pending",
    val expectedAmount: Int = 0,
    val amountPaid: Int = 0,
    val updatedAt: Timestamp = Timestamp.now()
)