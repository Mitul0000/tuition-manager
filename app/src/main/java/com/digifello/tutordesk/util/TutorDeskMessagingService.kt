package com.digifello.tutordesk.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.digifello.tutordesk.MainActivity
import com.digifello.tutordesk.R
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class TutorDeskMessagingService : FirebaseMessagingService() {

    // Called when Firebase gives this phone a new token
    override fun onNewToken(token: String) {
        saveTokenForCurrentUser(token)
    }

    // Called when a message arrives while the app is OPEN (foreground).
    // When the app is closed/background, Firebase shows the notification itself.
    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: getString(R.string.app_name)
        val body = message.notification?.body ?: message.data["body"] ?: return
        showNotification(title, body)
    }
    private fun loadLogo(): Bitmap? {
        val options = BitmapFactory.Options().apply { inSampleSize = 4 }   // shrink to about 313px
        return BitmapFactory.decodeResource(resources, R.drawable.logo_app, options)
    }
    private fun showNotification(title: String, body: String) {
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            manager.getNotificationChannel(CHANNEL_ID) == null
        ) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "General updates", NotificationManager.IMPORTANCE_HIGH)
            )
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )



        val largeIcon = loadLogo()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_tutordesk)               // small white icon
            .setLargeIcon(largeIcon)                                  // your coloured logo
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        if (NotificationHelper.canPostNotifications(this)) {
            manager.notify(Random.nextInt(), notification)
        }
    }

    companion object {
        const val CHANNEL_ID = "general_updates"

        fun saveTokenForCurrentUser(token: String) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .set(mapOf("fcmTokens" to FieldValue.arrayUnion(token)),
                    com.google.firebase.firestore.SetOptions.merge())
        }
    }
}