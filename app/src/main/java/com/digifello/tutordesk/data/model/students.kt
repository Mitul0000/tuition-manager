package com.digifello.tutordesk.data.model

import com.google.firebase.Timestamp


data class Student(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val batchId: String = "",
    val batchName: String = "",
    val isActive: Boolean = true,
    val joinedAt: Timestamp = Timestamp.now()
)