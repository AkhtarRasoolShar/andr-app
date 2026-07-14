import re

with open('app/src/main/java/com/example/network/SnowwhiteApiService.kt', 'r') as f:
    content = f.read()

target = """    val description: String? = null,
    val category: String? = null
)"""

replacement = """    val description: String? = null,
    val category: String? = null,
    @field:Json(name = "sub_category") val subCategory: String? = null
)"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/network/SnowwhiteApiService.kt', 'w') as f:
    f.write(content)
