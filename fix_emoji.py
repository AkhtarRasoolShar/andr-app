import re

with open('app/src/main/java/com/example/ui/screens/MarketScreens.kt', 'r') as f:
    content = f.read()

target = '                                            "Specialized" -> "🧥 Specialized"'
replacement = '                                            "Specialized" -> "🧥 Specialized"\n                                            "Pickup and Drop" -> "🚚 Pickup and Drop"'

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/MarketScreens.kt', 'w') as f:
    f.write(content)
