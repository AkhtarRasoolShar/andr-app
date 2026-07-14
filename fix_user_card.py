import re

with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'r') as f:
    content = f.read()

# Add localCtx to UserProfileCard
target = """fun UserProfileCard(
    user: UserProfile,
    allOrders: List<com.example.data.Order>,
    viewModel: MarketViewModel,
    onLogout: () -> Unit,
    onUpdatePreferences: (String) -> Unit,
    onNavigateToTab: (Int) -> Unit
) {"""

good = target + "\n    val localCtx = androidx.compose.ui.platform.LocalContext.current"

content = content.replace(target, good)

with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'w') as f:
    f.write(content)
