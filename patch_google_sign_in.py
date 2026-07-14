import re

with open('app/src/main/java/com/example/data/FirebaseAuthService.kt', 'r') as f:
    content = f.read()

new_method = """
    suspend fun signInWithGoogleCredential(idToken: String): String = suspendCoroutine { continuation ->
        val authInstance = auth
        if (authInstance == null) {
            continuation.resume("local_success")
            return@suspendCoroutine
        }
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        authInstance.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    Log.d(TAG, "Firebase Google sign-in successful: ${user?.email}")
                    continuation.resume(user?.uid ?: "success")
                } else {
                    val exStr = task.exception?.localizedMessage ?: "Google Authentication failed."
                    continuation.resumeWithException(Exception(exStr))
                }
            }
    }
"""

content = content.replace('fun signOutFirebase()', new_method + '\n    fun signOutFirebase()')

with open('app/src/main/java/com/example/data/FirebaseAuthService.kt', 'w') as f:
    f.write(content)
