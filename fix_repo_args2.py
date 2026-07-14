import re

with open('app/src/main/java/com/example/data/InventoryRepository.kt', 'r') as f:
    content = f.read()

target = """    suspend fun updateProductRemote(
        id: Int,
        title: String,
        price: Double,
        stockLeft: Int,
        imageUrl: String,
        description: String = "Premium Service",
        category: String = "Specialized"
    ): Boolean {
        val response = com.example.network.RetrofitClient.apiService.updateProduct(
            com.example.network.UpdateProductRequest(
                id = id,
                title = title,
                price = price,
                stockLeft = stockLeft,
                imageUrl = imageUrl,
                description = description,
                category = category
            )
        )"""

replacement = """    suspend fun updateProductRemote(
        id: Int,
        title: String,
        price: Double,
        stockLeft: Int,
        imageUrl: String,
        description: String = "Premium Service",
        category: String = "Specialized",
        subCategory: String? = null
    ): Boolean {
        val response = com.example.network.RetrofitClient.apiService.updateProduct(
            com.example.network.UpdateProductRequest(
                id = id,
                title = title,
                price = price,
                stockLeft = stockLeft,
                imageUrl = imageUrl,
                description = description,
                category = category,
                subCategory = subCategory
            )
        )"""

if target in content:
    content = content.replace(target, replacement)
else:
    print("Target not found")

with open('app/src/main/java/com/example/data/InventoryRepository.kt', 'w') as f:
    f.write(content)
