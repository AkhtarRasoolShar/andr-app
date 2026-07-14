import re

with open('app/src/main/java/com/example/data/InventoryRepository.kt', 'r') as f:
    content = f.read()

target = """                        category = res.category ?: "Specialized",
                        stock = res.stockLeft,"""

replacement = """                        category = res.category ?: "Specialized",
                        subCategory = res.subCategory,
                        stock = res.stockLeft,"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/data/InventoryRepository.kt', 'w') as f:
    f.write(content)
