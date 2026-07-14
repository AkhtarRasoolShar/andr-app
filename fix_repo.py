import re

with open('app/src/main/java/com/example/data/InventoryRepository.kt', 'r') as f:
    content = f.read()

target = """            dao.insertProfile(adminProfile)
            dao.markAllProfilesLoggedOutExcept(1)"""

replacement = """            dao.logoutAllUsers()
            dao.insertProfile(adminProfile)"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/data/InventoryRepository.kt', 'w') as f:
    f.write(content)
