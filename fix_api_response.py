import re

with open('app/src/main/java/com/example/network/SnowwhiteApiService.kt', 'r') as f:
    content = f.read()

target = """    val category: String? = null,
    val artisanName: String? = null,"""

replacement = """    val category: String? = null,
    @field:Json(name = "sub_category") val subCategory: String? = null,
    val artisanName: String? = null,"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/network/SnowwhiteApiService.kt', 'w') as f:
    f.write(content)
