import re

with open('app/src/main/java/com/example/ui/screens/AdminAppScreens.kt', 'r') as f:
    content = f.read()

target = """                                                listOf("Pending", "Processing", "Shipped", "Delivered", "Cancelled").forEach { status ->"""

replacement = """                                                listOf("Pending", "Picked Up", "In Process", "Out for Delivery", "Delivered", "Cancelled").forEach { status ->"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/AdminAppScreens.kt', 'w') as f:
    f.write(content)
