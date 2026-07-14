import re

with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'r') as f:
    content = f.read()

target1 = """                            "Completed", "Delivered" -> Color(0xFFE8F5E9)
                            "Processing" -> Color(0xFFFFF3E0)
                            else -> Color(0xFFE3F2FD)"""

replacement1 = """                            "Completed", "Delivered" -> Color(0xFFE8F5E9)
                            "Processing", "In Process", "Picked Up" -> Color(0xFFFFF3E0)
                            else -> Color(0xFFE3F2FD)"""

target2 = """                                "Completed", "Delivered" -> Color(0xFF2E7D32)
                                "Processing" -> Color(0xFFE65100)
                                else -> Color(0xFF1565C0)"""

replacement2 = """                                "Completed", "Delivered" -> Color(0xFF2E7D32)
                                "Processing", "In Process", "Picked Up" -> Color(0xFFE65100)
                                else -> Color(0xFF1565C0)"""

content = content.replace(target1, replacement1)
content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'w') as f:
    f.write(content)
