import re

with open('app/src/main/java/com/example/ui/screens/MarketScreens.kt', 'r') as f:
    content = f.read()

target1 = """            onConfirm = { title, desc, price, cat, stock, artisan, imgUrl ->
                viewModel.uploadProduct(
                    title = title,
                    price = price,
                    stockLeft = stock,
                    imageUrl = imgUrl,
                    description = desc,
                    category = cat
                ) { success, errorMsg ->"""

replacement1 = """            onConfirm = { title, desc, price, cat, subCat, stock, artisan, imgUrl ->
                viewModel.uploadProduct(
                    title = title,
                    price = price,
                    stockLeft = stock,
                    imageUrl = imgUrl,
                    description = desc,
                    category = cat,
                    subCategory = subCat
                ) { success, errorMsg ->"""

target2 = """            onConfirm = { title, desc, price, cat, stock, artisan, imgUrl ->
                viewModel.updateProductRemote(
                    id = orig.id,
                    title = title,
                    price = price,
                    stockLeft = stock,
                    imageUrl = imgUrl,
                    description = desc,
                    category = cat
                ) { success, errorMsg ->"""

replacement2 = """            onConfirm = { title, desc, price, cat, subCat, stock, artisan, imgUrl ->
                viewModel.updateProductRemote(
                    id = orig.id,
                    title = title,
                    price = price,
                    stockLeft = stock,
                    imageUrl = imgUrl,
                    description = desc,
                    category = cat,
                    subCategory = subCat
                ) { success, errorMsg ->"""

content = content.replace(target1, replacement1)
content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/ui/screens/MarketScreens.kt', 'w') as f:
    f.write(content)
