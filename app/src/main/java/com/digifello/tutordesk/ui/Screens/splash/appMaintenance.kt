package com.digifello.tutordesk.ui.Screens.splash

import com.digifello.tutordesk.data.repository.AuthRepository
import com.digifello.tutordesk.data.repository.BatchRepository
import com.digifello.tutordesk.data.repository.PaymentRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppMaintenance(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val authRepository: AuthRepository = AuthRepository(),
    private val batchRepository: BatchRepository = BatchRepository(),
    private val paymentRepository: PaymentRepository = PaymentRepository()
) {

    private fun currentMonthKey(): String {
        val formatter = SimpleDateFormat("yyyy-MM", Locale.US)
        return formatter.format(Date())
    }

    suspend fun runMonthlyRolloverIfNeeded() {
        val userId = authRepository.currentUserId ?: return
        val month = currentMonthKey()


        val batchesSnapshot = firestore.collection("users").document(userId)
            .collection("batches")
            .get()
            .await()

        for (batchDoc in batchesSnapshot.documents) {
            val batch = batchDoc.toObject(com.digifello.tutordesk.data.model.Batch::class.java) ?: continue
            if (batch.studentCount <= 0) continue

            val expectedAmount = batch.totalMoney / batch.studentCount

            // fetch every ACTIVE student in this batch
            val studentsSnapshot = firestore.collection("users").document(userId)
                .collection("students")
                .whereEqualTo("batchId", batch.id)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            for (studentDoc in studentsSnapshot.documents) {
                val studentId = studentDoc.id
                paymentRepository.seedMonthForStudent(studentId, month, expectedAmount)
            }
        }
    }
}