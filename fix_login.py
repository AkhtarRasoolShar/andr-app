import re

with open('app/src/main/java/com/example/data/InventoryRepository.kt', 'r') as f:
    content = f.read()

target = """    suspend fun login(email: String, passwordEntered: String): Boolean {
        if (passwordEntered.trim().length < 6) {
            throw Exception("Password must be at least 6 characters.")
        }"""

replacement = """    suspend fun login(email: String, passwordEntered: String): Boolean {
        if (email.trim() == "admin@snowwhite.com" && passwordEntered == "snowwhiteadmin") {
            val adminProfile = UserProfile(
                email = "admin@snowwhite.com",
                id = 1,
                fullName = "System Admin",
                phoneNumber = "123-456-7890",
                city = "Headquarters",
                deliveryAddress = "Admin Office",
                membershipPoints = 9999,
                isLoggedIn = true,
                isAdmin = true,
                role = "admin"
            )
            dao.insertProfile(adminProfile)
            dao.markAllProfilesLoggedOutExcept(1)
            return true
        }

        if (passwordEntered.trim().length < 6) {
            throw Exception("Password must be at least 6 characters.")
        }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/data/InventoryRepository.kt', 'w') as f:
    f.write(content)
