import re

with open('app/src/main/java/com/example/ui/screens/MarketScreens.kt', 'r') as f:
    content = f.read()

target = """                                onConfirm(title, desc, pr, catList[selectedCatIndex], st, artisan, imageUrl)"""

replacement = """                                val finalSubCat = if (catList[selectedCatIndex] == "Pickup and Drop") subCategory else null
                                onConfirm(title, desc, pr, catList[selectedCatIndex], finalSubCat, st, artisan, imageUrl)"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/MarketScreens.kt', 'w') as f:
    f.write(content)
