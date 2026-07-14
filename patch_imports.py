import re

with open('app/src/main/java/com/example/viewmodel/MarketViewModel.kt', 'r') as f:
    content = f.read()

imports = """
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
"""

content = content.replace('import kotlinx.coroutines.launch', 'import kotlinx.coroutines.launch' + imports)

with open('app/src/main/java/com/example/viewmodel/MarketViewModel.kt', 'w') as f:
    f.write(content)
