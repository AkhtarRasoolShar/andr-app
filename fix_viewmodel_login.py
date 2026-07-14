import re

with open('app/src/main/java/com/example/viewmodel/MarketViewModel.kt', 'r') as f:
    content = f.read()

target = """    fun login(email: String, passwordEntered: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val success = repository.login(email, passwordEntered)
                if (success) {
                    fetchAndUploadFcmToken()
                }
                onResult(success, if (success) null else "Invalid username or security combination.")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Sign-in error.")
            }
        }
    }"""

replacement = """    fun login(email: String, passwordEntered: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val success = repository.login(email, passwordEntered)
                if (success) {
                    fetchAndUploadFcmToken()
                    kotlinx.coroutines.delay(300) // Wait for Flow to emit new logged-in user
                }
                onResult(success, if (success) null else "Invalid username or security combination.")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Sign-in error.")
            }
        }
    }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/viewmodel/MarketViewModel.kt', 'w') as f:
    f.write(content)
