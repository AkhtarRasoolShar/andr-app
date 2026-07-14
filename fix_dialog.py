import re

with open('app/src/main/java/com/example/ui/screens/MarketScreens.kt', 'r') as f:
    content = f.read()

target = """    onConfirm: (String, String, Double, String, Int, String, String) -> Unit
) {
    var title by remember { mutableStateOf(product?.title ?: "") }
    var desc by remember { mutableStateOf(product?.description ?: "") }
    var priceStr by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var stockStr by remember { mutableStateOf(product?.stock?.toString() ?: "") }
    var artisan by remember { mutableStateOf(product?.artisanName ?: "") }
    var imageUrl by remember { mutableStateOf(product?.imageUrl ?: "") }"""

replacement = """    onConfirm: (String, String, Double, String, String?, Int, String, String) -> Unit
) {
    var title by remember { mutableStateOf(product?.title ?: "") }
    var desc by remember { mutableStateOf(product?.description ?: "") }
    var priceStr by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var stockStr by remember { mutableStateOf(product?.stock?.toString() ?: "") }
    var artisan by remember { mutableStateOf(product?.artisanName ?: "") }
    var imageUrl by remember { mutableStateOf(product?.imageUrl ?: "") }
    var subCategory by remember { mutableStateOf(product?.subCategory ?: "") }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/MarketScreens.kt', 'w') as f:
    f.write(content)
