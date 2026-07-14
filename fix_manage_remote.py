import re

with open('app/src/main/java/com/example/viewmodel/MarketViewModel.kt', 'r') as f:
    content = f.read()

target = """        category: String? = null,
        onResult: (Boolean, String?) -> Unit"""

replacement = """        category: String? = null,
        subCategory: String? = null,
        onResult: (Boolean, String?) -> Unit"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/viewmodel/MarketViewModel.kt', 'w') as f:
    f.write(content)
