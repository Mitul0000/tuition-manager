package com.digifello.tuitionmanager.data.repository

import com.digifello.tuitionmanager.data.model.Payment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PaymentRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun paymentsRef(batchId: String) =
        firestore.collection("batches").document(batchId).collection("payments")

    fun getPaymentForMonth(batchId: String, month: String): Flow<Payment?> = callbackFlow {
        val listener = paymentsRef(batchId).document(month)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.toObject(Payment::class.java))
            }
        awaitClose { listener.remove() }
    }

    suspend fun getCurrentMonthPayments(batchIds: List<String>, month: String): List<Payment> {
        return batchIds.mapNotNull { batchId ->
            paymentsRef(batchId).document(month).get().await().toObject(Payment::class.java)
        }
    }

    /**
     * All unpaid/partial payments across every batch, any month.
     * Powers Finance screen's "Total Due" section.
     */
    suspend fun getAllOutstandingPayments(): List<Payment> {
        val snapshot = firestore.collectionGroup("payments")
            .whereIn("status", listOf("pending", "partial"))
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(Payment::class.java) }
    }

    suspend fun ensureCurrentMonthPayment(batchId: String, batchName: String, month: String, expectedAmount: Double) {
        val docRef = paymentsRef(batchId).document(month)
        val existing = docRef.get().await()
        if (!existing.exists()) {
            val payment = Payment(
                month = month,
                status = "pending",
                expectedAmount = expectedAmount,
                amountPaid = 0.0,
                paidOn = null,
                batchId = batchId,
                batchName = batchName
            )
            docRef.set(payment).await()
        }
    }

    suspend fun cleanupPaidRecords(batchId: String) {
        val paidDocs = paymentsRef(batchId).whereEqualTo("status", "paid").get().await()
        paidDocs.documents.forEach { it.reference.delete().await() }
    }

    suspend fun updatePaymentAmount(
        batchId: String,
        batchName: String,
        month: String,
        amountPaid: Double,
        expectedAmount: Double
    ) {
        val status = when {
            amountPaid <= 0.0 -> "pending"
            amountPaid < expectedAmount -> "partial"
            else -> "paid"
        }
        paymentsRef(batchId).document(month).set(
            Payment(
                month = month,
                status = status,
                expectedAmount = expectedAmount,
                amountPaid = amountPaid,
                paidOn = Timestamp.now(),
                batchId = batchId,
                batchName = batchName
            )
        ).await()
    }
}