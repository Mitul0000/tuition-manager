package com.digifello.tuitionmanager.data.repository

import com.digifello.tuitionmanager.data.model.Batch
import com.digifello.tuitionmanager.data.model.Student
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BatchRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val batchesRef = firestore.collection("batches")

    /**
     * Live stream of all batches, ordered by name.
     * Used by Dashboard and Today screens — updates automatically
     * whenever data changes in Firestore (no manual refresh needed).
     */
    fun getBatchesFlow(): Flow<List<Batch>> = callbackFlow {
        val listener = batchesRef
            .orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)   // propagate error to collector
                    return@addSnapshotListener
                }
                val batches = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Batch::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(batches)
            }
        awaitClose { listener.remove() }   // stops listening when no longer needed
    }

    /**
     * One-time fetch of all batches (not live).
     * Used by the monthly payment maintenance routine, which just needs
     * a snapshot to loop through once — not a live UI stream.
     */
    suspend fun getBatchesOnce(): List<Batch> {
        val snapshot = batchesRef.get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Batch::class.java)?.copy(id = doc.id)
        }
    }

    /**
     * Fetch a single batch by ID. Used by Batch Detail screen.
     */
    suspend fun getBatch(batchId: String): Batch? {
        val doc = batchesRef.document(batchId).get().await()
        return doc.toObject(Batch::class.java)?.copy(id = doc.id)
    }

    /**
     * Creates a new batch document, then adds all its students as
     * documents in the batch's "students" subcollection.
     * Returns the new batch's generated ID.
     */
    suspend fun addBatch(batch: Batch, students: List<Student>): String {
        val newBatchRef = batchesRef.document()   // generates a new ID up front
        val batchWithId = batch.copy(id = newBatchRef.id)

        newBatchRef.set(batchWithId).await()

        val studentsRef = newBatchRef.collection("students")
        students.forEach { student ->
            val newStudentRef = studentsRef.document()
            val studentWithIds = student.copy(
                id = newStudentRef.id,
                batchId = newBatchRef.id,
                batchName = batch.name
            )
            newStudentRef.set(studentWithIds).await()
        }

        return newBatchRef.id
    }

    /**
     * Updates a batch's own fields. If the name changed, also updates
     * "batchName" on every student in this batch, since that field is
     * denormalized (copied) onto each student document for the
     * All Students screen — see Student.kt comments.
     */
    suspend fun updateBatch(batch: Batch) {
        batchesRef.document(batch.id).set(batch).await()

        val studentsRef = batchesRef.document(batch.id).collection("students")
        val studentDocs = studentsRef.get().await()
        studentDocs.documents.forEach { doc ->
            studentsRef.document(doc.id).update("batchName", batch.name).await()
        }
    }

    /**
     * Deletes a batch and everything under it: its students subcollection
     * and its payments subcollection. Firestore does NOT cascade-delete
     * subcollections automatically, so each must be cleared manually.
     */
    suspend fun deleteBatch(batchId: String) {
        val batchRef = batchesRef.document(batchId)

        val studentsSnapshot = batchRef.collection("students").get().await()
        studentsSnapshot.documents.forEach { it.reference.delete().await() }

        val paymentsSnapshot = batchRef.collection("payments").get().await()
        paymentsSnapshot.documents.forEach { it.reference.delete().await() }

        batchRef.delete().await()
    }
}