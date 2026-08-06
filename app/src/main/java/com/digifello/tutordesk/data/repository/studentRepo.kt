package com.digifello.tutordesk.data.repository

import com.digifello.tutordesk.data.model.Student
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class StudentRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val authRepository: AuthRepository = AuthRepository()
) {

    private fun userDoc(uid: String) = firestore.collection("users").document(uid)

    suspend fun createStudent(student: Student, batchId: String): String {
        val userId = authRepository.currentUserId ?: return ""

        val studentDocRef = userDoc(userId).collection("students").document()
        val batchDocRef = userDoc(userId).collection("batches").document(batchId)

        val studentWithIds = student.copy(
            id = studentDocRef.id,
            batchId = batchId
        )

        firestore.runTransaction { transaction ->
            transaction.set(studentDocRef, studentWithIds)
            transaction.update(batchDocRef, "studentCount", FieldValue.increment(1))
        }.await()

        return studentDocRef.id
    }

    suspend fun updateStudent(student: Student) {
        val userId = authRepository.currentUserId ?: return
        val studentDoc = userDoc(userId)
            .collection("students")
            .document(student.id)
        studentDoc.update(
            "name", student.name,
            "phone", student.phone
        ).await()
    }

    suspend fun deleteStudent(student: Student) {
        val userId = authRepository.currentUserId ?: return
        val studentDocRef = userDoc(userId).collection("students").document(student.id)
        val batchDocRef = if (student.batchId.isNotBlank()) {
            userDoc(userId).collection("batches").document(student.batchId)
        } else null

        firestore.runTransaction { transaction ->
            // All reads first — Firestore transactions require this before any writes.
            val batchSnapshot = batchDocRef?.let { transaction.get(it) }

            transaction.delete(studentDocRef)

            if (batchDocRef != null && batchSnapshot != null) {
                val currentTotalMoney = batchSnapshot.getLong("totalMoney")?.toInt() ?: 0
                val currentStudentCount = batchSnapshot.getLong("studentCount")?.toInt() ?: 0

                // This student's equal share of the batch fee, based on the
                // count *before* removal. Subtracting exactly this amount
                // keeps every remaining student's per-head share unchanged:
                // (T - T/N) / (N - 1) == T/N.
                val share = if (currentStudentCount > 0) currentTotalMoney / currentStudentCount else 0

                transaction.update(
                    batchDocRef,
                    mapOf(
                        "studentCount" to FieldValue.increment(-1),
                        "totalMoney" to FieldValue.increment(-share.toLong())
                    )
                )
            }
        }.await()
    }

    suspend fun getStudentById(studentId: String): Student? {
        val userId = authRepository.currentUserId ?: return null
        val snapshot = userDoc(userId).collection("students").document(studentId).get().await()
        return snapshot.toObject(Student::class.java)
    }

    fun getStudents(): Flow<List<Student>> = callbackFlow {
        val userId = authRepository.currentUserId ?: run {
            close()
            return@callbackFlow
        }

        val listener = userDoc(userId)
            .collection("students")
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    close(exception)
                    return@addSnapshotListener
                }
                val students = snapshots?.documents?.mapNotNull {
                    it.toObject(Student::class.java)
                } ?: emptyList()
                trySend(students)
            }

        awaitClose { listener.remove() }
    }

    fun getStudentsForBatch(batchId: String): Flow<List<Student>> = callbackFlow {
        val userId = authRepository.currentUserId ?: run {
            close()
            return@callbackFlow
        }

        val listener = userDoc(userId)
            .collection("students")
            .whereEqualTo("batchId", batchId)
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    close(exception)
                    return@addSnapshotListener
                }
                val students = snapshots?.documents?.mapNotNull {
                    it.toObject(Student::class.java)
                } ?: emptyList()
                trySend(students)
            }

        awaitClose { listener.remove() }
    }
}