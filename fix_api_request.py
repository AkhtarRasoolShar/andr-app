import re

with open('app/src/main/java/com/example/network/SnowwhiteApiService.kt', 'r') as f:
    content = f.read()

target1 = """    val description: String = "Premium Service",
    val category: String = "Specialized"
)"""

replacement1 = """    val description: String = "Premium Service",
    val category: String = "Specialized",
    @field:Json(name = "sub_category") val subCategory: String? = null
)"""

target2 = """    val description: String,
    val category: String
)"""

replacement2 = """    val description: String,
    val category: String,
    @field:Json(name = "sub_category") val subCategory: String? = null
)"""

target3 = """    val category: String?,
    @field:Json(name = "created_at") val createdAt: String?"""

replacement3 = """    val category: String?,
    @field:Json(name = "sub_category") val subCategory: String?,
    @field:Json(name = "created_at") val createdAt: String?"""

content = content.replace(target1, replacement1)
content = content.replace(target2, replacement2)
content = content.replace(target3, replacement3)

with open('app/src/main/java/com/example/network/SnowwhiteApiService.kt', 'w') as f:
    f.write(content)
