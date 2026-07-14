import re

with open('app/src/main/java/com/example/data/FirestoreService.kt', 'r') as f:
    content = f.read()

target = """                                    val category = docData.getString("category") ?: ""
                                    val stock = docData.getLong("stock")?.toInt() ?: 0"""

replacement = """                                    val category = docData.getString("category") ?: ""
                                    val subCategory = docData.getString("subCategory")
                                    val stock = docData.getLong("stock")?.toInt() ?: 0"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/data/FirestoreService.kt', 'w') as f:
    f.write(content)
