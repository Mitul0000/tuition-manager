package com.digifello.tutordesk.data.repository

import com.digifello.tutordesk.data.model.Batch
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BatchRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val authRepository: AuthRepository = AuthRepository()
) {
    suspend fun createBatch(batch: Batch): String {
        val userId = authRepository.currentUserId ?: return ""
        val batchDoc = firestore.collection("users").document(userId)
            .collection("batches").document()
        val batchWithId = batch.copy(
            id = batchDoc.id
        )
        batchDoc.set(
            batchWithId
        ).await()
        return batchDoc.id
    }

    suspend fun updateBatch(batch: Batch) {
        val userId = authRepository.currentUserId ?: return
        val batchesDoc = firestore.collection("users").document(userId)
            .collection("batches")
            .document(batch.id)
        batchesDoc.update(
            "name", batch.name,
            "totalMoney", batch.totalMoney,
            "days", batch.days,
            "time", batch.time
        ).await()
    }

    suspend fun deleteBatch(batch: Batch) {
        val userId = authRepository.currentUserId ?: return

        val userDocRef = firestore.collection("users").document(userId)
        val batchDocRef = userDocRef.collection("batches").document(batch.id)

        val affectedStudents = userDocRef.collection("students")
            .whereEqualTo("batchId", batch.id)
            .get()
            .await()

        val writeBatch = firestore.batch()
        writeBatch.delete(batchDocRef)

        for (doc in affectedStudents.documents) {
            writeBatch.update(
                doc.reference,
                mapOf(
                    "batchId" to "",
                    "batchName" to "",
                    "isActive" to false
                )
            )
        }

        writeBatch.commit().await()
    }



    fun getBatch(batchId: String): Flow<Batch?> = callbackFlow {
        val userId = authRepository.currentUserId ?: run {
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users")
        .document(userId)
        .collection("batches")
        .document(batchId)
        .addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                close(exception)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(Batch::class.java))
        }

        awaitClose {
            listener.remove()
        }
    }

    fun getBatches(): Flow<List<Batch>> = callbackFlow {
        val userId = authRepository.currentUserId ?: run {
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users")
            .document(userId)
            .collection("batches")
            .addSnapshotListener { snapshots, exception ->

                if (exception != null) {
                    close(exception)
                    return@addSnapshotListener
                }

                val batches = snapshots?.documents?.mapNotNull {
                    it.toObject(Batch::class.java)
                } ?: emptyList()

                trySend(batches)
            }
        awaitClose {
            listener.remove()
        }
    }
}