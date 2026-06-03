package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object FirebaseAuthService {
    private const val TAG = "FirebaseAuthService"

    private val _isConfigured = MutableStateFlow(false)
    val isConfigured: StateFlow<Boolean> = _isConfigured.asStateFlow()

    private val _authStatusMessage = MutableStateFlow("Firebase Authentication initializing...")
    val authStatusMessage: StateFlow<String> = _authStatusMessage.asStateFlow()

    private var auth: FirebaseAuth? = null

    fun initialize(context: Context) {
        try {
            val apps = FirebaseApp.getApps(context)
            val app = if (apps.isEmpty()) {
                FirebaseApp.initializeApp(context)
            } else {
                apps[0]
            }

            if (app != null) {
                auth = FirebaseAuth.getInstance()
                _isConfigured.value = true
                _authStatusMessage.value = "Firebase Secure Auth Controller Active."
                Log.d(TAG, "FirebaseAuth initialized successfully.")
            } else {
                _isConfigured.value = false
                _authStatusMessage.value = "Auth offline (FirebaseApp null). Using local Room user schema."
            }
        } catch (e: Exception) {
            _isConfigured.value = false
            _authStatusMessage.value = "Secure authentication running in local sandboxed database."
            Log.e(TAG, "FirebaseAuth initialization failed: ${e.localizedMessage}")
        }
    }

    fun getCurrentUserEmail(): String? {
        return auth?.currentUser?.email
    }

    /**
     * Creates user account using Google Firebase Authenication.
     */
    suspend fun createUserWithFirebase(email: String, passwordEntered: String): String = suspendCoroutine { continuation ->
        val authInstance = auth
        if (authInstance == null) {
            continuation.resume("local_success")
            return@suspendCoroutine
        }

        authInstance.createUserWithEmailAndPassword(email.trim(), passwordEntered.trim())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    Log.d(TAG, "Firebase account created successfully: ${user?.email}")
                    continuation.resume(user?.uid ?: "success")
                } else {
                    val exStr = task.exception?.localizedMessage ?: "Sign-up failed."
                    continuation.resumeWithException(Exception(exStr))
                }
            }
    }

    /**
     * Signs in user account using Google Firebase Authentication.
     */
    suspend fun signInWithFirebase(email: String, passwordEntered: String): String = suspendCoroutine { continuation ->
        val authInstance = auth
        if (authInstance == null) {
            continuation.resume("local_success")
            return@suspendCoroutine
        }

        authInstance.signInWithEmailAndPassword(email.trim(), passwordEntered.trim())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    Log.d(TAG, "Firebase sign-in successful: ${user?.email}")
                    continuation.resume(user?.uid ?: "success")
                } else {
                    val exStr = task.exception?.localizedMessage ?: "Authentication failed."
                    continuation.resumeWithException(Exception(exStr))
                }
            }
    }

    /**
     * Signs out from Firebase Authentication.
     */
    fun signOutFirebase() {
        try {
            auth?.signOut()
            Log.d(TAG, "Firebase user logged out successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error when signing out from Firebase: ${e.localizedMessage}")
        }
    }
}
