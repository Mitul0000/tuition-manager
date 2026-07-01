package com.digifello.tuitionmanager.data.model

/**
 * Represents one student, stored as a document inside a batch's
 * "students" subcollection: batches/{batchId}/students/{studentId}
 */
data class Student(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val description: String? = null,   // optional, nullable — matches "optional" requirement
    val batchId: String = "",          // denormalized: same as parent batch's id
    val batchName: String = ""         // denormalized: avoids extra lookups on All Students screen
)