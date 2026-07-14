import re

with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'r') as f:
    content = f.read()

# Remove the line with LocalContext inside onClick
bad = """                                    val localCtx = androidx.compose.ui.platform.LocalContext.current
                                    val sessionManager = com.example.data.SessionManager(localCtx)"""
good = """                                    val sessionManager = com.example.data.SessionManager(localCtx)"""
content = content.replace(bad, good)

with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'w') as f:
    f.write(content)

