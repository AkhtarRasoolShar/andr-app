import re

with open('app/src/main/java/com/example/data/FirestoreService.kt', 'r') as f:
    content = f.read()

target1 = """                                    val category = doc.getString("category") ?: "Specialized"
                                    val stock = doc.getLong("stock")?.toInt() ?: 0"""

replacement1 = """                                    val category = doc.getString("category") ?: "Specialized"
                                    val subCategory = doc.getString("subCategory")
                                    val stock = doc.getLong("stock")?.toInt() ?: 0"""

target2 = """                                    val p = Product(id = pId, title = title, description = desc, price = price, category = category, stock = stock, artisanName = artisan, imageUrl = img, rating = 5.0)"""

replacement2 = """                                    val p = Product(id = pId, title = title, description = desc, price = price, category = category, subCategory = subCategory, stock = stock, artisanName = artisan, imageUrl = img, rating = 5.0)"""

content = content.replace(target1, replacement1)
content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/data/FirestoreService.kt', 'w') as f:
    f.write(content)
