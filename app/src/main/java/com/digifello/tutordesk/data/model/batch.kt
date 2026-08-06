package com.digifello.tutordesk.data.model

import com.google.firebase.Timestamp

data class Batch (
    val id:String = "",
    val name: String="",
    val days: List<String> = emptyList(),
    val totalMoney:Int = 0,
    val time:String = "",
    val studentCount: Int = 0,
    val createdAt: Timestamp = Timestamp.now()
)