import re

with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'r') as f:
    content = f.read()

target1 = """import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers"""

replacement1 = """import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.example.BuildConfig"""

target2 = """                    if (!isCreatingState && sessionManager.isBiometricEnabled() && sessionManager.getCachedEmail() != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                        Button("""

replacement2 = """                    if (!isCreatingState && sessionManager.isBiometricEnabled() && sessionManager.getCachedEmail() != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                        Button("""

# Wait, we need a separate Google button regardless of biometric state.
# We can add it at the bottom.

target3 = """                                fontWeight = FontWeight.Bold
                            )
                        }
                    }"""

replacement3 = """                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    val coroutineScope = rememberCoroutineScope()
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val credentialManager = CredentialManager.create(localCtx)
                                    val googleIdOption = GetGoogleIdOption.Builder()
                                        .setFilterByAuthorizedAccounts(false)
                                        .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                                        .setAutoSelectEnabled(true)
                                        .build()

                                    val request = GetCredentialRequest.Builder()
                                        .addCredentialOption(googleIdOption)
                                        .build()

                                    val result = credentialManager.getCredential(localCtx, request)
                                    val credential = result.credential

                                    if (credential is androidx.credentials.CustomCredential &&
                                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                        val email = googleIdTokenCredential.id
                                        val displayName = googleIdTokenCredential.displayName ?: "Google User"
                                        
                                        // Login
                                        sessionManager.cacheSecureSession(email, displayName, "customer")
                                        viewModel.autoLoginFromCache(email, displayName, "customer")
                                        successMsg = "Google Sign-In Successful"
                                        onNavigateToTab(0)
                                    } else {
                                        formError = "Unexpected credential type"
                                    }
                                } catch (e: Exception) {
                                    formError = "Google Sign-In Error: ${e.localizedMessage}"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_myplaces), "Google icon", modifier = Modifier.size(20.dp), tint = Color.Unspecified)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Sign in with Google", fontWeight = FontWeight.Bold)
                    }"""

content = content.replace(target1, replacement1)
content = content.replace(target3, replacement3)

with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'w') as f:
    f.write(content)
