package com.digifello.tuitionmanager.data.repository

import com.digifello.tuitionmanager.data.model.Student
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class StudentRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /**
     * Live stream of every student across ALL batches, flattened into one list.
     * Uses a "collection group" query — this looks across every "students"
     * subcollection in the whole database, regardless of which batch it's under.
     * Powers the All Students screen.
     */
    fun getAllStudentsFlow(): Flow<List<Student>> = callbackFlow {
        val listener = firestore.collectionGroup("students")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val students = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Student::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(students)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Live stream of students belonging to one specific batch.
     * Used by Batch Detail screen's roster section.
     */
    fun getStudentsForBatch(batchId: String): Flow<List<Student>> = callbackFlow {
        val listener = firestore.collection("batches")
            .document(batchId)
            .collection("students")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val students = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Student::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(students)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Adds a single student to a batch's roster.
     * Used when editing a batch and adding one more student after creation.
     */
    suspend fun addStudent(batchId: String, batchName: String, student: Student) {
        val studentsRef = firestore.collection("batches")
            .document(batchId)
            .collection("students")
        val newRef = studentsRef.document()
        val studentWithIds = student.copy(id = newRef.id, batchId = batchId, batchName = batchName)
        newRef.set(studentWithIds).await()
    }

    /**
     * Updates one student's own details (name, phone, description).
     */
    suspend fun updateStudent(batchId: String, student: Student) {
        firestore.collection("batches")
            .document(batchId)
            .collection("students")
            .document(student.id)
            .set(student)
            .await()
    }

    /**
     * Removes one student from a batch's roster.
     */
    suspend fun deleteStudent(batchId: String, studentId: String) {
        firestore.collection("batches")
            .document(batchId)
            .collection("students")
            .document(studentId)
            .delete()
            .await()
    }
}