package com.example.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.network.RetrofitClient
import kotlinx.coroutines.*

class ChatForegroundService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val CHANNEL_ID = "ChatForegroundChannel"
    
    private var previousActiveChatsHash = -1

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Support Chat Active")
            .setContentText("Listening for real-time chat messages...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }

        startPolling()

        return START_STICKY
    }

    private fun startPolling() {
        val sharedPrefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        scope.launch {
            while (isActive) {
                try {
                    // Quick check if there is an active session
                    val currentRole = sharedPrefs.getString("role", null)
                    if (currentRole == "admin") {
                        val chatRes = RetrofitClient.apiService.getActiveChats()
                        if (chatRes.isSuccessful && chatRes.body()?.success == true) {
                            val currentChats = chatRes.body()?.chats ?: emptyList()
                            val currentStateHash = currentChats.hashCode()
                            
                            if (previousActiveChatsHash != -1 && currentStateHash != previousActiveChatsHash) {
                                com.example.utils.NotificationHelper.sendChatNotification(
                                    this@ChatForegroundService, "New Chat Message \uD83D\uDCAC", "A customer has sent a new message."
                                )
                                com.example.utils.SoundHelper.playChatSound(this@ChatForegroundService)
                            }
                            previousActiveChatsHash = currentStateHash
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(3000)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Chat Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
