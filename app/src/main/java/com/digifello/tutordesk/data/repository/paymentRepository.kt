package com.digifello.tutordesk.data.repository

import com.digifello.tutordesk.data.model.Payment
import com.digifello.tutordesk.data.model.Student
import com.digifello.tutordesk.util.currentMonthKey
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

fun computePaymentStatus(amountPaid: Int, expectedAmount: Int): String {
    return when {
        expectedAmount > 0 && amountPaid >= expectedAmount -> "paid"
        amountPaid > 0 -> "partial"
        else -> "pending"
    }
}

class PaymentRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val authRepository: AuthRepository = AuthRepository()
) {

    private fun userDoc(uid: String) = firestore.collection("users").document(uid)

    private fun paymentDoc(uid: String, studentId: String, month: String) =
        userDoc(uid)
            .collection("students").document(studentId)
            .collection("payments").document(month)


    suspend fun seedMonthForStudent(studentId: String, month: String, expectedAmount: Int) {
        val userId = authRepository.currentUserId ?: return
        val ref = paymentDoc(userId, studentId, month)
        val existing = ref.get().await()
        if (!existing.exists()) {
            ref.set(
                Payment(
                    month = month,
                    status = "pending",
                    expectedAmount = expectedAmount,
                    amountPaid = 0
                )
            ).await()
        }
    }

    fun observePaymentsForStudents(studentIds: List<String>, month: String): Flow<Map<String, Payment?>> = callbackFlow {
        val userId = authRepository.currentUserId ?: run { close(); return@callbackFlow }

        if (studentIds.isEmpty()) {
            trySend(emptyMap())
            awaitClose { }
            return@callbackFlow
        }

        val current = mutableMapOf<String, Payment?>()
        val listeners = studentIds.map { studentId ->
            paymentDoc(userId, studentId, month).addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                current[studentId] = snapshot?.toObject(Payment::class.java)?.copy(month = month)
                trySend(current.toMap())
            }
        }

        awaitClose { listeners.forEach { it.remove() } }
    }

    suspend fun updatePaymentForAllStudent(batchId: String,expectedAmount: Int){
        val userId = authRepository.currentUserId ?: return
        val writeBatch = firestore.batch()
        val BatchStudents = userDoc(userId).collection("students").whereEqualTo("batchId",batchId).get().await()
        for (student in BatchStudents){
            val ref = paymentDoc(userId, student.id, currentMonthKey())
            val snapshot = ref.get().await()
            if (!snapshot.exists()) continue
            writeBatch.set(
                ref,
                mapOf(
                    "expectedAmount" to expectedAmount
                ),
                SetOptions.merge()
            )

        }
        writeBatch.commit().await()
    }


    suspend fun markPaymentByStudent(studentId: String, month: String, newAmountPaid: Int) {
        val userId = authRepository.currentUserId ?: return
        val ref = paymentDoc(userId, studentId, month)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            val previousAmountPaid = snapshot.getLong("amountPaid")?.toInt() ?: 0
            val expectedAmount = snapshot.getLong("expectedAmount")?.toInt() ?: 0
            val delta = (newAmountPaid - previousAmountPaid).toLong()

            transaction.set(
                ref,
                mapOf(
                    "amountPaid" to newAmountPaid,
                    "status" to computePaymentStatus(newAmountPaid, expectedAmount),
                    "expectedAmount" to expectedAmount,
                    "updatedAt" to Timestamp.now()
                ),
                SetOptions.merge()
            )

            if (delta != 0L) {

                transaction.set(
                    userDoc(userId),
                    mapOf("totalEarnedAllTime" to FieldValue.increment(delta)),
                    SetOptions.merge()
                )
            }
        }.await()
    }


    suspend fun markPaymentByBatch(batchId: String, month: String) {
        val userId = authRepository.currentUserId ?: return

        val studentsSnapshot = userDoc(userId)
            .collection("students")
            .whereEqualTo("batchId", batchId)
            .get()
            .await()

        val writeBatch = firestore.batch()
        var totalDelta = 0L

        for (doc in studentsSnapshot.documents) {
            val student = doc.toObject(Student::class.java) ?: continue
            val ref = paymentDoc(userId, student.id, month)

            val paymentSnapshot = ref.get().await()
            if (!paymentSnapshot.exists()) continue

            val previousAmountPaid = paymentSnapshot.getLong("amountPaid")?.toInt() ?: 0
            val expectedAmount = paymentSnapshot.getLong("expectedAmount")?.toInt() ?: 0
            val delta = (expectedAmount - previousAmountPaid).toLong()
            totalDelta += delta

            writeBatch.set(
                ref,
                mapOf(
                    "amountPaid" to expectedAmount,
                    "status" to "paid",
                    "expectedAmount" to expectedAmount,
                    "updatedAt" to Timestamp.now()
                ),
                SetOptions.merge()
            )
        }

        if (totalDelta != 0L) {
            writeBatch.set(
                userDoc(userId),
                mapOf("totalEarnedAllTime" to  FieldValue.increment(totalDelta)),
                SetOptions.merge()
            )
        }

        writeBatch.commit().await()
    }


    fun getPaymentHistoryForStudent(studentId: String): Flow<List<Payment>> = callbackFlow {
        val userId = authRepository.currentUserId ?: run {
            close()
            return@callbackFlow
        }

        val listener = userDoc(userId)
            .collection("students").document(studentId)
            .collection("payments")
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    close(exception)
                    return@addSnapshotListener
                }
                val payments = snapshots?.documents?.mapNotNull { doc ->
                    doc.toObject(Payment::class.java)?.copy(month = doc.id)
                } ?: emptyList()
                trySend(payments)
            }

        awaitClose { listener.remove() }
    }

    suspend fun getPaymentsForStudents(studentIds: List<String>, month: String): Map<String, Payment?> {
        val userId = authRepository.currentUserId ?: return emptyMap()
        return coroutineScope {
            studentIds.map { studentId ->
                async {
                    val snapshot = paymentDoc(userId, studentId, month).get().await()
                    studentId to snapshot.toObject(Payment::class.java)?.copy(month = month)
                }
            }.awaitAll().toMap()
        }
    }

    fun getTotalEarnedAllTime(): Flow<Long> = callbackFlow {
        val userId = authRepository.currentUserId ?: run {
            close()
            return@callbackFlow
        }

        val listener = userDoc(userId)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    close(exception)
                    return@addSnapshotListener
                }
                trySend(snapshot?.getLong("totalEarnedAllTime") ?: 0L)
            }

        awaitClose { listener.remove() }
    }
}