import re

with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'val sessionManager = com.example.data.SessionManager(androidx.compose.ui.platform.LocalContext.current)',
    'val context = androidx.compose.ui.platform.LocalContext.current\n    val sessionManager = com.example.data.SessionManager(context)'
)

with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'w') as f:
    f.write(content)
