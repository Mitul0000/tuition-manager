package com.digifello.tutordesk.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.digifello.tutordesk.R
import kotlin.random.Random

object NotificationHelper {

    private const val CHANNEL_ID = "paper_downloads"
    private const val CHANNEL_NAME = "Paper downloads"

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val existing = manager.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifies you when a generated question paper or answer key finishes downloading"
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    /** True if we're allowed to post notifications right now (always true below API 33). */
    fun canPostNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    /**
     * Shows a "download complete" notification for [fileName]. Tapping it opens the PDF
     * with the system's PDF viewer via [contentUri], same as any other download-manager style flow.
     */
    fun showDownloadComplete(context: Context, fileName: String, contentUri: Uri) {
        if (!canPostNotifications(context)) return
        ensureChannel(context)

        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "application/pdf")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            Random.nextInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Download complete")
            .setContentText(fileName)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$fileName is ready. Tap to open."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = ContextCompat.getSystemService(context, NotificationManager::class.java)
        manager?.notify(Random.nextInt(), notification)
    }
}