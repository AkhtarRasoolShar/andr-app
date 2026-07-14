import re

with open('app/src/main/java/com/example/data/InventoryRepository.kt', 'r') as f:
    content = f.read()

target1 = """    suspend fun uploadProduct(
        title: String,
        price: Double,
        stockLeft: Int,
        imageUrl: String,
        description: String = "Premium Service",
        category: String = "Specialized"
    ): Boolean {
        val response = com.example.network.RetrofitClient.apiService.addProduct(
            com.example.network.AddProductRequest(
                title = title,
                price = price,
                stockLeft = stockLeft,
                imageUrl = imageUrl,
                description = description,
                category = category
            )
        )"""

replacement1 = """    suspend fun uploadProduct(
        title: String,
        price: Double,
        stockLeft: Int,
        imageUrl: String,
        description: String = "Premium Service",
        category: String = "Specialized",
        subCategory: String? = null
    ): Boolean {
        val response = com.example.network.RetrofitClient.apiService.addProduct(
            com.example.network.AddProductRequest(
                title = title,
                price = price,
                stockLeft = stockLeft,
                imageUrl = imageUrl,
                description = description,
                category = category,
                subCategory = subCategory
            )
        )"""

target2 = """    suspend fun updateProductRemote(
        id: Int,
        title: String,
        price: Double,
        stockLeft: Int,
        imageUrl: String,
        description: String,
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

replacement2 = """    suspend fun updateProductRemote(
        id: Int,
        title: String,
        price: Double,
        stockLeft: Int,
        imageUrl: String,
        description: String,
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

content = content.replace(target1, replacement1)
content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/data/InventoryRepository.kt', 'w') as f:
    f.write(content)
