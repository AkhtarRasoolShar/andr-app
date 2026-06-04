package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Json

data class UserSession(
    val userId: Int,
    val name: String,
    val email: String,
    val role: String
)

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("snowwhite_user_session", Context.MODE_PRIVATE)

    fun saveSession(userId: Int, name: String, email: String, role: String) {
        prefs.edit().apply {
            putInt("user_id", userId)
            putString("name", name)
            putString("email", email)
            putString("role", role)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    fun fetchSession(): UserSession? {
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        if (!isLoggedIn) return null
        
        val userId = prefs.getInt("user_id", 1)
        val name = prefs.getString("name", "") ?: ""
        val email = prefs.getString("email", "") ?: ""
        val role = prefs.getString("role", "customer") ?: "customer"
        return UserSession(userId, name, email, role)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
