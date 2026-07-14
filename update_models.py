import re

with open('app/src/main/java/com/example/data/Models.kt', 'r') as f:
    content = f.read()

target = """    val category: String, // e.g., "Dry Cleaning", "Laundry", "Carpet & Rugs", "Specialized"
    val stock: Int,       // Real-time inventory"""

replacement = """    val category: String, // e.g., "Dry Cleaning", "Laundry", "Carpet & Rugs", "Specialized"
    val subCategory: String? = null,
    val stock: Int,       // Real-time inventory"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/data/Models.kt', 'w') as f:
    f.write(content)
