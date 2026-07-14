import re

with open('app/src/main/java/com/example/viewmodel/MarketViewModel.kt', 'r') as f:
    content = f.read()

target1 = """    fun uploadProduct(
        title: String,
        price: Double,
        stockLeft: Int,
        imageUrl: String,
        description: String = "Premium Service",
        category: String = "Specialized",
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val success = repository.uploadProduct(
                    title = title,
                    price = price,
                    stockLeft = stockLeft,
                    imageUrl = imageUrl,
                    description = description,
                    category = category
                )"""

replacement1 = """    fun uploadProduct(
        title: String,
        price: Double,
        stockLeft: Int,
        imageUrl: String,
        description: String = "Premium Service",
        category: String = "Specialized",
        subCategory: String? = null,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val success = repository.uploadProduct(
                    title = title,
                    price = price,
                    stockLeft = stockLeft,
                    imageUrl = imageUrl,
                    description = description,
                    category = category,
                    subCategory = subCategory
                )"""

target2 = """    fun updateProductRemote(
        id: Int,
        title: String,
        price: Double,
        stockLeft: Int,
        imageUrl: String,
        description: String,
        category: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val success = repository.updateProductRemote(
                    id = id,
                    title = title,
                    price = price,
                    stockLeft = stockLeft,
                    imageUrl = imageUrl,
                    description = description,
                    category = category
                )"""

replacement2 = """    fun updateProductRemote(
        id: Int,
        title: String,
        price: Double,
        stockLeft: Int,
        imageUrl: String,
        description: String,
        category: String,
        subCategory: String? = null,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val success = repository.updateProductRemote(
                    id = id,
                    title = title,
                    price = price,
                    stockLeft = stockLeft,
                    imageUrl = imageUrl,
                    description = description,
                    category = category,
                    subCategory = subCategory
                )"""

content = content.replace(target1, replacement1)
content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/viewmodel/MarketViewModel.kt', 'w') as f:
    f.write(content)
