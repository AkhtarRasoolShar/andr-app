import re

with open('app/src/main/java/com/example/viewmodel/MarketViewModel.kt', 'r') as f:
    content = f.read()

target = """                    val p = com.example.data.Product(
                        id = pId,
                        title = title ?: "",
                        description = description ?: "",
                        price = price ?: 0.0,
                        category = category ?: "",
                        stock = stock ?: 0,
                        artisanName = "Admin",
                        imageUrl = imageUrl ?: "",
                        rating = 5.0
                    )"""

replacement = """                    val p = com.example.data.Product(
                        id = pId,
                        title = title ?: "",
                        description = description ?: "",
                        price = price ?: 0.0,
                        category = category ?: "",
                        subCategory = subCategory,
                        stock = stock ?: 0,
                        artisanName = "Admin",
                        imageUrl = imageUrl ?: "",
                        rating = 5.0
                    )"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/viewmodel/MarketViewModel.kt', 'w') as f:
    f.write(content)
