package com.example.utils

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri

object SoundHelper {
    private var mediaPlayer: MediaPlayer? = null

    fun playChatSound(context: Context) {
        val sharedPrefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val chatSoundEnabled = sharedPrefs.getBoolean("chat_sound_enabled", true)
        if (!chatSoundEnabled) return
        
        val customUriStr = sharedPrefs.getString("chat_ringtone_uri", "")
        val soundUri = if (!customUriStr.isNullOrEmpty()) {
            Uri.parse(customUriStr)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
        
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, soundUri)
                setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                }
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
