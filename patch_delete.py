import re

with open('app/src/main/java/com/example/viewmodel/MarketViewModel.kt', 'r') as f:
    content = f.read()

new_del = """    fun deleteProductRemote(productId: Int, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteProductRemote(productId)
                com.example.data.FirestoreService.deleteProductInCloud(productId)
                loadProductsFromApi()
                withContext(Dispatchers.Main) { onResult(true, null) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onResult(false, e.localizedMessage ?: "Product deletion failure.") }
            }
        }
    }"""

content = re.sub(r'    fun deleteProductRemote.*?\}\n        \}\n    \}', new_del, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/viewmodel/MarketViewModel.kt', 'w') as f:
    f.write(content)
