import re

with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'r') as f:
    content = f.read()

google_btn = """
                                            Spacer(modifier = Modifier.height(16.dp))
                                            val context = androidx.compose.ui.platform.LocalContext.current
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
                                            }
"""

content = content.replace('android.widget.Toast.makeText(context, formError, android.widget.Toast.LENGTH_LONG).show()\n                                                    }\n                                                }\n                                            }', 'android.widget.Toast.makeText(context, formError, android.widget.Toast.LENGTH_LONG).show()\n                                                    }\n                                                }\n                                            }\n' + google_btn)

with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'w') as f:
    f.write(content)
