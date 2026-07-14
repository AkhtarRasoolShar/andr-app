import re

with open('app/src/main/java/com/example/ui/screens/MarketScreens.kt', 'r') as f:
    content = f.read()

bad = '"Dry Cleaning", "Laundry", "Carpet "Dry Cleaning", "Laundry", "Carpet & Rugs", "Specialized" Rugs", "Specialized", "Pickup and Drop"'
good = '"Dry Cleaning", "Laundry", "Carpet & Rugs", "Specialized", "Pickup and Drop"'

content = content.replace(bad, good)

with open('app/src/main/java/com/example/ui/screens/MarketScreens.kt', 'w') as f:
    f.write(content)
