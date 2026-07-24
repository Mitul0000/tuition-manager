package com.digifello.tuitionmanager.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.digifello.tuitionmanager.ui.theme.DotFri
import com.digifello.tuitionmanager.ui.theme.DotMon
import com.digifello.tuitionmanager.ui.theme.DotSat
import com.digifello.tuitionmanager.ui.theme.DotSun
import com.digifello.tuitionmanager.ui.theme.DotThu
import com.digifello.tuitionmanager.ui.theme.DotTue
import com.digifello.tuitionmanager.ui.theme.DotWed
import com.digifello.tuitionmanager.ui.theme.Hairline

// TutorDesk's signature repeating element.
// activeDays: set of 1..7 (Calendar.MONDAY..SUNDAY-style, Monday = 1)
// Reused identically on batch cards, Routine's day selector, and paper metadata.

private val weekdayColors = listOf(
    DotMon, DotTue, DotWed, DotThu, DotFri, DotSat, DotSun
)
private val weekdayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

@Composable
fun WeekdayDotStrip(
    activeDays: Set<Int>, // 1 = Monday ... 7 = Sunday
    modifier: Modifier = Modifier,
    dotSize: androidx.compose.ui.unit.Dp = 8.dp,
    spacing: androidx.compose.ui.unit.Dp = 6.dp,
    showLabels: Boolean = false
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (day in 1..7) {
            val isActive = activeDays.contains(day)
            val color = if (isActive) weekdayColors[day - 1] else Color.Transparent

            if (showLabels) {
                androidx.compose.material3.Text(
                    text = weekdayLabels[day - 1],
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = if (isActive) weekdayColors[day - 1] else Hairline
                )
            } else {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(color)
                        .border(1.dp, if (isActive) color else Hairline, CircleShape)
                )
            }
        }
    }
}