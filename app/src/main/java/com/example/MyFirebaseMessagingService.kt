package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.network.FcmTokenRequest
import com.example.network.RetrofitClient
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: "Snowwhite Alert"
        val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body ?: "You have a new update."
        
        sendNotification(title, body)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        val sessionManager = com.example.data.SessionManager(applicationContext)
        val session = sessionManager.fetchSession()
        if (session != null && session.userId > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    RetrofitClient.apiService.updateFcmToken(
                        FcmTokenRequest(userId = session.userId, fcmToken = token)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun sendNotification(title: String, body: String) {
        val sharedPrefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val notificationsEnabled = sharedPrefs.getBoolean("notifications_enabled", true)
        if (!notificationsEnabled) return
        
        val soundEnabled = sharedPrefs.getBoolean("notification_sound_enabled", true)
        val soundPreset = sharedPrefs.getString("notification_sound_preset", "Default") ?: "Default"

        val soundUri = if (soundEnabled) {
            when (soundPreset) {
                "Digital" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                "Chime" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                "Bell" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
        } else {
            null
        }

        val channelIdPrefix = "snowwhite_fcm_channel_"
        val channelId = if (soundEnabled) "${channelIdPrefix}${soundPreset.lowercase()}" else "${channelIdPrefix}silent"

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            
        if (soundUri != null) {
            notificationBuilder.setSound(soundUri)
            try {
                val r = RingtoneManager.getRingtone(applicationContext, soundUri)
                r?.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Snowwhite Notifications",
                if (soundEnabled) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                if (soundUri != null) {
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                    setSound(soundUri, audioAttributes)
                } else {
                    setSound(null, null)
                }
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}
