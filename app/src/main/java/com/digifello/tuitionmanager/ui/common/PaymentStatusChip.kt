package com.digifello.tuitionmanager.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.digifello.tuitionmanager.ui.theme.PaidGreen
import com.digifello.tuitionmanager.ui.theme.PaidGreenBg
import com.digifello.tuitionmanager.ui.theme.PartialAmber
import com.digifello.tuitionmanager.ui.theme.PartialAmberBg
import com.digifello.tuitionmanager.ui.theme.UnpaidCrimson
import com.digifello.tuitionmanager.ui.theme.UnpaidCrimsonBg

enum class PaymentStatus { PAID, PARTIAL, UNPAID }

// The single source of truth for payment-status color + label across the whole app.
// Every screen (Home batch cards, Batch Detail rows, Finance) must use this —
// never inline a status color directly.

@Composable
fun PaymentStatusChip(
    status: PaymentStatus,
    modifier: Modifier = Modifier
) {
    val (bg, fg, label) = when (status) {
        PaymentStatus.PAID -> Triple(PaidGreenBg, PaidGreen, "Paid")
        PaymentStatus.PARTIAL -> Triple(PartialAmberBg, PartialAmber, "Partial")
        PaymentStatus.UNPAID -> Triple(UnpaidCrimsonBg, UnpaidCrimson, "Unpaid")
    }

    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = fg,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

// Maps an amount paid vs. expected to a status. Used by BatchDetail/Finance
// so the Paid/Partial/Unpaid rule lives in exactly one place.
fun computePaymentStatus(amountPaid: Double, amountExpected: Double): PaymentStatus = when {
    amountExpected <= 0.0 -> PaymentStatus.UNPAID
    amountPaid <= 0.0 -> PaymentStatus.UNPAID
    amountPaid >= amountExpected -> PaymentStatus.PAID
    else -> PaymentStatus.PARTIAL
}