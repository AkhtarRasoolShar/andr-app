import re

# 1. MarketViewModel
with open('app/src/main/java/com/example/viewmodel/MarketViewModel.kt', 'r') as f:
    content = f.read()
content = content.replace('repository.addProductLocal(p)', 'repository.insertProduct(p)')
with open('app/src/main/java/com/example/viewmodel/MarketViewModel.kt', 'w') as f:
    f.write(content)

# 2. FirestoreService
with open('app/src/main/java/com/example/data/FirestoreService.kt', 'r') as f:
    content = f.read()
content = content.replace('dao.deleteProduct(pId)', 'dao.getProductById(pId)?.let { dao.deleteProduct(it) }')
with open('app/src/main/java/com/example/data/FirestoreService.kt', 'w') as f:
    f.write(content)

# 3. ProfileScreen localcontext
with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'r') as f:
    content = f.read()
# Replace where context is defined inside the Button block
content = content.replace('val context = androidx.compose.ui.platform.LocalContext.current', '')
# Ensure LocalContext is at the top of the composable, wait it already is at the top of ProfileScreen
with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'w') as f:
    f.write(content)

