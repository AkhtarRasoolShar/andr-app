import re

with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'r') as f:
    content = f.read()

# 1. Fix the context being missing around line 957 and elsewhere
# Let's ensure context is defined properly at the very top of ProfileScreen.
# Actually I did put it there: val context = androidx.compose.ui.platform.LocalContext.current
# But wait, ProfileScreen composable might not span to line 957.
# ProfileScreen is probably calling other composables, e.g. EditProfileDialog, which don't have context defined!
# If so, they need their own `val context = LocalContext.current`.

content = content.replace(
    'val sessionManager = com.example.data.SessionManager(context)\n                                    val session = sessionManager.fetchSession()',
    'val context = androidx.compose.ui.platform.LocalContext.current\n                                    val sessionManager = com.example.data.SessionManager(context)\n                                    val session = sessionManager.fetchSession()'
)

# 2. Fix the Google Sign-in button placement.
# It was placed inside onClick. I will remove it from there.
bad_btn = """                                            Spacer(modifier = Modifier.height(16.dp))
                                            
                                            val coroutineScope = rememberCoroutineScope()
                                            OutlinedButton(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        try {
                                                            val credentialManager = androidx.credentials.CredentialManager.create(context)
                                                            // We need a web client id. We will use a placeholder or check BuildConfig
                                                            val webClientId = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com" // Update in production
                                                            val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                                                                .setFilterByAuthorizedAccounts(false)
                                                                .setServerClientId(webClientId)
                                                                .setAutoSelectEnabled(true)
                                                                .build()
                                                            val request = androidx.credentials.GetCredentialRequest.Builder()
                                                                .addCredentialOption(googleIdOption)
                                                                .build()
                                                            val result = credentialManager.getCredential(context, request)
                                                            val credential = result.credential
                                                            if (credential is androidx.credentials.CustomCredential && credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                                                val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                                                                val idToken = googleIdTokenCredential.idToken
                                                                val uid = com.example.data.FirebaseAuthService.signInWithGoogleCredential(idToken)
                                                                val email = googleIdTokenCredential.id
                                                                successMsg = "Successfully authenticated with Google!"
                                                                formError = null
                                                                // Sync user profile
                                                                viewModel.login(email, "google-sso") { success, _ -> 
                                                                    if (viewModel.loggedInUser.value?.isAdmin == true) {
                                                                        onNavigateToTab(11)
                                                                    } else {
                                                                        onNavigateToTab(0)
                                                                    }
                                                                }
                                                            }
                                                        } catch (e: Exception) {
                                                            formError = "Google Sign In Failed: ${e.localizedMessage}. Please configure Web Client ID."
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Email,
                                                    contentDescription = "Google Icon",
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Sign in with Google", fontWeight = FontWeight.Bold)
                                            }"""

content = content.replace(bad_btn, '')

# Add it after the Button block in the main column. 
# Search for `Button(\n                                onClick = {\n                                    if (isCreatingState) {` and go down to its closing.
# I'll just write it before the closing of the Column that contains the login form.
good_btn = """                                            Spacer(modifier = Modifier.height(16.dp))
                                            val coroutineScope = rememberCoroutineScope()
                                            OutlinedButton(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        try {
                                                            val credentialManager = androidx.credentials.CredentialManager.create(context)
                                                            val webClientId = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"
                                                            val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                                                                .setFilterByAuthorizedAccounts(false)
                                                                .setServerClientId(webClientId)
                                                                .setAutoSelectEnabled(true)
                                                                .build()
                                                            val request = androidx.credentials.GetCredentialRequest.Builder()
                                                                .addCredentialOption(googleIdOption)
                                                                .build()
                                                            val result = credentialManager.getCredential(context, request)
                                                            val credential = result.credential
                                                            if (credential is androidx.credentials.CustomCredential && credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                                                val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                                                                val uid = com.example.data.FirebaseAuthService.signInWithGoogleCredential(googleIdTokenCredential.idToken)
                                                                viewModel.login(googleIdTokenCredential.id, "google-sso") { _, _ ->
                                                                    onNavigateToTab(if (viewModel.loggedInUser.value?.isAdmin == true) 11 else 0)
                                                                }
                                                            }
                                                        } catch (e: Exception) {
                                                            formError = "Google Sign In Failed: ${e.localizedMessage}"
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth().height(50.dp)
                                            ) {
                                                Text("Sign in with Google")
                                            }"""

# Find the end of the Button block for login.
target = """                                        }
                                    }
                                )
                            }
                        }"""
content = content.replace(target, target + "\n" + good_btn)

with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'w') as f:
    f.write(content)
