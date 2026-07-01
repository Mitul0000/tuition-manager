package com.digifello.tuitionmanager.data.repository

import com.digifello.tuitionmanager.util.DateUtils

/**
 * Runs the monthly payment lifecycle (Step 8 from the architecture doc):
 * deletes resolved ("paid") records, then ensures a fresh "pending"
 * record exists for the current month, for every batch.
 * Called once per app open.
 */
class AppMaintenance(
    private val batchRepository: BatchRepository = BatchRepository(),
    private val paymentRepository: PaymentRepository = PaymentRepository()
) {
    suspend fun runMonthlyPaymentMaintenance() {
        val currentMonth = DateUtils.currentMonthKey()
        val batches = batchRepository.getBatchesOnce()
        batches.forEach { batch ->
            paymentRepository.cleanupPaidRecords(batch.id)
            paymentRepository.ensureCurrentMonthPayment(
                batchId = batch.id,
                batchName = batch.name,
                month = currentMonth,
                expectedAmount = batch.totalMoney
            )
        }
    }
}