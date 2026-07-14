import re

with open('app/src/main/java/com/example/ui/screens/MarketScreens.kt', 'r') as f:
    content = f.read()

target = """            onSave = { title, price, stock, imageUrl, description, category ->
                viewModel.manageProductRemote(
                    action = if (isEdit) "edit" else "add",
                    productId = product?.id,
                    title = title,
                    price = price,
                    stock = stock,
                    imageUrl = imageUrl,
                    description = description,
                    category = category
                ) { success, errorMsg ->"""

replacement = """            onSave = { title, price, stock, imageUrl, description, category, subCategory ->
                viewModel.manageProductRemote(
                    action = if (isEdit) "edit" else "add",
                    productId = product?.id,
                    title = title,
                    price = price,
                    stock = stock,
                    imageUrl = imageUrl,
                    description = description,
                    category = category,
                    subCategory = subCategory
                ) { success, errorMsg ->"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/MarketScreens.kt', 'w') as f:
    f.write(content)
