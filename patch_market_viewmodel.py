import re

with open('app/src/main/java/com/example/viewmodel/MarketViewModel.kt', 'r') as f:
    content = f.read()

# Replace manageProductRemote
new_manage = """    fun manageProductRemote(
        action: String,
        productId: Int? = null,
        title: String? = null,
        price: Double? = null,
        stock: Int? = null,
        imageUrl: String? = null,
        description: String? = null,
        category: String? = null,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (action == "delete" && productId != null) {
                    repository.deleteProductRemote(productId)
                    com.example.data.FirestoreService.deleteProductInCloud(productId)
                    loadProductsFromApi()
                    withContext(Dispatchers.Main) { onResult(true, "Product deleted.") }
                } else {
                    val pId = productId ?: (System.currentTimeMillis() % 1000000).toInt()
                    val p = com.example.data.Product(
                        id = pId,
                        title = title ?: "",
                        description = description ?: "",
                        price = price ?: 0.0,
                        category = category ?: "",
                        stock = stock ?: 0,
                        artisanName = "Admin",
                        imageUrl = imageUrl ?: "",
                        rating = 5.0
                    )
                    repository.addProductLocal(p)
                    com.example.data.FirestoreService.addOrUpdateProductInCloud(p)
                    loadProductsFromApi()
                    withContext(Dispatchers.Main) { onResult(true, "Service saved.") }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onResult(false, e.localizedMessage ?: "API Error") }
            }
        }
    }"""

content = re.sub(r'    fun manageProductRemote\(.*?onResult\(false, e\.localizedMessage \?: "API Error"\)\n            \}\n        \}\n    \}', new_manage, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/viewmodel/MarketViewModel.kt', 'w') as f:
    f.write(content)
