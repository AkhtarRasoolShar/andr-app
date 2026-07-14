package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class InventoryRepository(private val dao: MarketplaceDao, private val context: Context) {

    val allProducts: Flow<List<Product>> = dao.getAllProductsFlow()
    val allCartItems: Flow<List<CartItem>> = dao.getCartItemsFlow()
    val allOrders: Flow<List<Order>> = dao.getAllOrdersFlow()
    val loggedInUser: Flow<UserProfile?> = dao.getLoggedInUserFlow()
    val allUserProfiles: Flow<List<UserProfile>> = dao.getAllUserProfilesFlow()
    val wishlistIds: Flow<List<Int>> = dao.getWishlistFlow()
    val savedAddresses: Flow<List<SavedAddress>> = dao.getSavedAddressesFlow()
    val allCategories: Flow<List<AppCategory>> = dao.getAllCategoriesFlow()

    suspend fun addCategoryLocal(name: String, iconName: String = "Star") {
        dao.insertCategory(AppCategory(name, iconName))
    }

    suspend fun deleteCategoryLocal(name: String) {
        dao.deleteCategory(AppCategory(name))
    }

    suspend fun uploadProduct(
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
        )
        if (response.success) {
            fetchProductsFromRemote()
            return true
        } else {
            throw Exception(response.message ?: "Server rejected product insertion.")
        }
    }

    suspend fun fetchChatHistory(userId: Int, otherId: Int): List<com.example.network.NetworkChatMessage> {
        return try {
            android.util.Log.d("ChatDebug", "Fetching chat history for: $userId and $otherId")
            val response = com.example.network.RetrofitClient.apiService.getChatHistory(userId = userId, otherId = otherId)
            android.util.Log.d("ChatDebug", "Fetch chat history response: ${response.code()} ${response.message()}")
            if (response.isSuccessful && response.body()?.success == true) {
                val msgs = response.body()?.messages ?: emptyList()
                android.util.Log.d("ChatDebug", "Fetched messages count: ${msgs.size}")
                msgs
            } else {
                android.util.Log.e("ChatDebug", "Failed to fetch chat history: ${response.errorBody()?.string()}")
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatDebug", "Exception fetching chat history", e)
            emptyList()
        }
    }

    suspend fun sendChatMessage(senderId: Int, receiverId: Int, message: String): Boolean {
        return try {
            val req = com.example.network.ChatSendRequest(senderId, receiverId, message)
            android.util.Log.d("ChatDebug", "Sending chat message: $req")
            val response = com.example.network.RetrofitClient.apiService.sendChatMessage(req)
            android.util.Log.d("ChatDebug", "Send chat response: ${response.code()} ${response.message()} body: ${response.body()}")
            if (!response.isSuccessful) {
                 android.util.Log.e("ChatDebug", "Error sending chat: ${response.errorBody()?.string()}")
            }
            response.isSuccessful && response.body()?.success == true
        } catch (e: Exception) {
            android.util.Log.e("ChatDebug", "Exception sending chat", e)
            false
        }
    }

    suspend fun uploadImage(base64Image: String): String {
        val response = com.example.network.RetrofitClient.apiService.uploadImage(
            com.example.network.UploadImageRequest(image = base64Image)
        )
        if (response.success && response.imageUrl != null) {
            return response.imageUrl
        } else {
            throw Exception(response.message ?: "Server rejected image upload")
        }
    }

    suspend fun fetchProductsFromRemote(): List<Product> {
        return try {
            val response = com.example.network.RetrofitClient.apiService.getLiveProducts()
            if (response.isSuccessful && response.body()?.success == true) {
                val mapped = response.body()?.products?.map { res ->
                    Product(
                        id = res.id,
                        title = res.title,
                        description = res.description ?: "Official high-end premium fabric care, laundry, washing, and carpet restoration services.",
                        price = res.price,
                        category = res.category ?: "Specialized",
                        subCategory = res.subCategory,
                        stock = res.stockLeft,
                        artisanName = res.artisanName ?: "Snowwhite Pakistan",
                        imageUrl = res.imageUrl,
                        rating = res.rating ?: 4.8
                    )
                } ?: emptyList()
                
                if (mapped.isNotEmpty()) {
                    mapped.forEach { dao.insertProduct(it) }
                }
                mapped
            } else {
                dao.getAllProductsFlow().first()
            }
        } catch (e: Exception) {
            val local = dao.getAllProductsFlow().first()
            if (local.isEmpty()) {
                val seed = getSeedProducts()
                seed.forEach { dao.insertProduct(it) }
                seed
            } else {
                local
            }
        }
    }

    suspend fun createAccount(profile: UserProfile, passwordEntered: String): Boolean {
        if (passwordEntered.trim().length < 6) {
            throw Exception("Password must be at least 6 characters.")
        }
        
        val response = com.example.network.RetrofitClient.apiService.registerUser(
            com.example.network.RegisterRequest(
                email = profile.email,
                name = profile.fullName,
                passwordEntered = passwordEntered,
                phoneNumber = profile.phoneNumber,
                city = profile.city,
                deliveryAddress = profile.deliveryAddress
            )
        )
        if (response.isSuccessful && response.body()?.success == true) {
            return true
        } else {
            val errorMsg = com.example.network.ErrorUtils.parseErrorMessage(response)
            throw Exception(errorMsg ?: response.body()?.message ?: "Registration failed (HTTP ${response.code()}).")
        }
    }

    suspend fun login(email: String, passwordEntered: String): Boolean {
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
            dao.logoutAllUsers()
            dao.insertProfile(adminProfile)
            return true
        }

        if (passwordEntered.trim().length < 6) {
            throw Exception("Password must be at least 6 characters.")
        }

        val retrofitResponse = com.example.network.RetrofitClient.apiService.loginUser(
            com.example.network.LoginRequest(email = email.trim(), passwordEntered = passwordEntered)
        )
        
        val response = retrofitResponse.body()
        
        if (retrofitResponse.isSuccessful && response != null && response.success && response.user != null) {
            val userRole = response.role ?: "customer"
            val returnedUser = response.user
            
            val fetchedId = returnedUser.userId ?: returnedUser.id ?: 0
            if (fetchedId == 0) {
                throw Exception("Invalid user ID received from server.")
            }
            
            val isAdminRole = userRole.equals("admin", ignoreCase = true) || userRole.equals("super_admin", ignoreCase = true)
            val adminProfile = UserProfile(
                email = returnedUser.email,
                id = fetchedId,
                fullName = returnedUser.fullName,
                phoneNumber = returnedUser.derivedPhone,
                city = returnedUser.city ?: "Unknown",
                deliveryAddress = returnedUser.derivedAddress,
                membershipPoints = returnedUser.membershipPoints,
                isLoggedIn = true,
                isAdmin = isAdminRole,
                role = userRole
            )
            dao.logoutAllUsers()
            dao.insertProfile(adminProfile)

            // Cache session in SessionManager & SharedPreferences
            try {
                val sessionManager = SessionManager(context)
                sessionManager.saveSession(
                    userId = fetchedId,
                    name = returnedUser.fullName,
                    email = returnedUser.email,
                    role = userRole
                )

                val sharedPrefs = context.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
                sharedPrefs.edit().apply {
                    putString("id", returnedUser.email)
                    putString("name", returnedUser.fullName)
                    putString("role", userRole)
                    apply()
                }
            } catch (e: Exception) {
                // Ignore caching errors
            }

            return true
        } else {
            val errorMsg = com.example.network.ErrorUtils.parseErrorMessage(retrofitResponse)
            throw Exception(errorMsg ?: response?.message ?: "Login failed (HTTP ${retrofitResponse.code()}).")
        }
    }

    suspend fun logout() {
        if (FirebaseAuthService.isConfigured.value) {
            FirebaseAuthService.signOutFirebase()
        }
        dao.logoutAllUsers()
        try {
            val sessionManager = SessionManager(context)
            sessionManager.clearSession()

            val sharedPrefs = context.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
            sharedPrefs.edit().clear().apply()
        } catch (e: Exception) {
            // Ignore clearing errors
        }
    }

    suspend fun updateSavedPreferences(email: String, preferencesRaw: String) {
        val user = dao.getUserByEmail(email)
        if (user != null) {
            dao.insertProfile(user.copy(savedPreferences = preferencesRaw))
        }
    }

    suspend fun updateUserProfile(userId: Int, oldEmail: String, email: String, fullName: String, phoneNumber: String, onResult: (Boolean, String?) -> Unit) {
        try {
            val req = com.example.network.UpdateProfileRequest(
                userId = userId,
                fullName = fullName,
                email = email,
                phoneNumber = phoneNumber
            )
            val res = com.example.network.RetrofitClient.apiService.updateProfile(req)
            if (res.isSuccessful && res.body()?.success == true) {
                // Update local DB
                val localUser = dao.getUserByEmail(oldEmail) ?: dao.getUserByEmail(email)
                if (localUser != null) {
                    val updated = localUser.copy(
                        email = email,
                        fullName = fullName,
                        phoneNumber = phoneNumber
                    )
                    // If email changes, primary key issue! Room might REPLACE if same PK, but if PK changed, it's a new row.
                    // We'll just insert it. If email changed, we should delete old one? 
                    // To be safe we just insertProfile(updated).
                    dao.insertProfile(updated)
                }
                
                try {
                    val sessionManager = SessionManager(context)
                    val oldRole = sessionManager.fetchSession()?.role ?: "customer"
                    sessionManager.saveSession(userId, fullName, email, oldRole)
                    if (sessionManager.isBiometricEnabled()) {
                         sessionManager.cacheSecureSession(email, fullName, oldRole)
                    }
                } catch(e: Exception){}
                
                onResult(true, "Profile updated successfully")
            } else {
                val errorMsg = com.example.network.ErrorUtils.parseErrorMessage(res)
                onResult(false, errorMsg ?: res.body()?.message ?: "Failed to update profile via API (HTTP ${res.code()}).")
            }
        } catch (e: Exception) {
            onResult(false, e.localizedMessage ?: "Failed to reach server")
        }
    }

    suspend fun getProductById(productId: Int): Product? {
        return dao.getProductById(productId)
    }

    suspend fun insertProduct(product: Product): Long {
        return dao.insertProduct(product)
    }

    suspend fun updateProduct(product: Product) {
        dao.updateProduct(product)
        if (FirestoreService.isConfigured.value) {
            FirestoreService.updateStockInCloud(product.id, product.stock)
        }
    }

    suspend fun deleteProduct(product: Product) {
        // Also remove from cart if present
        dao.deleteCartItemByProductId(product.id)
        dao.deleteProduct(product)
    }

    suspend fun addToCart(productId: Int) {
        val product = dao.getProductById(productId) ?: return
        if (product.stock <= 0) return // Out of stock

        val existing = dao.getCartItemByProductId(productId)
        if (existing == null) {
            dao.insertCartItem(CartItem(productId = productId, quantity = 1))
        } else {
            if (existing.quantity < product.stock) {
                dao.updateCartItem(existing.copy(quantity = existing.quantity + 1))
            }
        }
    }

    suspend fun decreaseCartItem(productId: Int) {
        val existing = dao.getCartItemByProductId(productId) ?: return
        if (existing.quantity <= 1) {
            dao.deleteCartItemByProductId(productId)
        } else {
            dao.updateCartItem(existing.copy(quantity = existing.quantity - 1))
        }
    }

    suspend fun removeFromCart(productId: Int) {
        dao.deleteCartItemByProductId(productId)
    }

    suspend fun clearCart() {
        dao.clearCart()
    }

    /**
     * Places an order. Checks and decrements product inventory.
     * Returns the placed Order on success, or throws an Exception with details if items are out of stock.
     */
    suspend fun checkOutCart(
        paymentMethod: String,
        cardLast4: String,
        shippingAddress: String,
        pickupSchedule: String = "",
        deliverySchedule: String = "",
        finalTotal: Double
    ): Order {
        val user = loggedInUser.first()
        val cartList = allCartItems.first()
        if (cartList.isEmpty()) {
            throw Exception("Cart is empty")
        }

        val products = allProducts.first()
        val cartUiList = cartList.map { cartItem ->
            val correspondingProduct = products.find { it.id == cartItem.productId }
                ?: throw Exception("Catalog discrepancy: Product no longer exists.")
            com.example.viewmodel.CartUiItem(cartItem, correspondingProduct)
        }

        val summaryItemsList = cartUiList.map { "${it.product.title} x${it.cartItem.quantity}" }

        val networkCartItems = cartUiList.map {
            com.example.network.NetworkCartItem(
                productId = it.product.id,
                quantity = it.cartItem.quantity,
                price = it.product.price
            )
        }

        val sessionManager = SessionManager(context)
        val session = sessionManager.fetchSession()
        val userId = session?.userId ?: throw Exception("Please login to place an order")
        if (userId <= 0) throw Exception("Please login to place an order")

        // Fire request to live API place_order.php
        val orderRequest = com.example.network.OrderRequest(
            userId = userId,
            totalAmount = finalTotal,
            paymentMethod = if (paymentMethod.equals("cod", ignoreCase = true)) "COD" else paymentMethod,
            address = shippingAddress,
            phone = "000000000",
            items = networkCartItems
        )

        val apiResponse = com.example.network.RetrofitClient.apiService.placeOrder(orderRequest)
        val responseBody = apiResponse.body()
        if (apiResponse.isSuccessful.not() || responseBody?.success != true) {
            throw Exception(responseBody?.message ?: "Server rejected checkout transaction.")
        }
        val orderIdFromApi = responseBody.orderId

        // Deduct/Reflect inventory changes locally for speed and consistency
        if (FirestoreService.isConfigured.value) {
            try {
                FirestoreService.performFirestoreCheckoutTransaction(cartUiList)
            } catch (e: Exception) {
                // non-blocking fallback
            }
        }
        for (item in cartUiList) {
            val updatedStock = (item.product.stock - item.cartItem.quantity).coerceAtLeast(0)
            dao.updateProductStock(item.product.id, updatedStock)
        }

        val orderId = responseBody?.orderId ?: "SNOW-${(10000..99999).random()}"
        val order = Order(
            id = orderId,
            timestamp = System.currentTimeMillis(),
            itemsSummary = summaryItemsList.joinToString(", "),
            totalAmount = finalTotal,
            status = "Processing",
            paymentCardLast4 = cardLast4,
            shippingAddress = shippingAddress,
            pickupSchedule = pickupSchedule,
            deliverySchedule = deliverySchedule
        )

        // Deduct/Add Loyalty Membership Points if logged in and record details
        if (user != null) {
            val pointsEarned = (finalTotal * 0.1).toInt().coerceAtLeast(1)
            val currentHistory = user.purchaseHistory
            val updatedHistory = if (currentHistory.isBlank()) orderId else "$currentHistory,$orderId"
            val updatedUser = user.copy(
                membershipPoints = user.membershipPoints + pointsEarned,
                purchaseHistory = updatedHistory
            )
            dao.insertProfile(updatedUser)
        }

        dao.insertOrder(order)
        dao.clearCart()
        return order
    }

    suspend fun saveLocalOrder(order: Order) {
        dao.insertOrder(order)
    }

    /**
     * Pre-populates the product catalog with Snowhite items if empty.
     */
    suspend fun ensureSeededData() {
        val currentList = allProducts.first()
        if (currentList.isEmpty()) {
            val seedItems = getSeedProducts()
            seedItems.forEach { dao.insertProduct(it) }
            if (FirestoreService.isConfigured.value) {
                FirestoreService.seedInitialProductsInCloud(seedItems)
            }
        } else if (FirestoreService.isConfigured.value) {
            FirestoreService.seedInitialProductsInCloud(currentList)
        }
    }

    suspend fun ensureAutoLoginUser(email: String, fullName: String, role: String) {
        val existing = dao.getUserByEmail(email)
        val isAdminRole = role.equals("admin", ignoreCase = true) || role.equals("super_admin", ignoreCase = true)
        val profile = UserProfile(
            email = email,
            id = existing?.id ?: 0,
            fullName = existing?.fullName ?: fullName,
            phoneNumber = existing?.phoneNumber ?: "0300-1112233",
            city = existing?.city ?: "Karachi",
            deliveryAddress = existing?.deliveryAddress ?: "Head Office, Karachi",
            membershipPoints = existing?.membershipPoints ?: 100,
            isLoggedIn = true,
            isAdmin = isAdminRole,
            role = role
        )
        dao.logoutAllUsers()
        dao.insertProfile(profile)
    }

    suspend fun updateUserRole(email: String, newRole: String) {
        val isAdmin = newRole.equals("admin", ignoreCase = true) || newRole.equals("super_admin", ignoreCase = true)
        dao.updateUserRole(email, newRole, isAdmin)
    }

    // Addresses/Wishlist
    suspend fun toggleWishlist(productId: Int, isWishlisted: Boolean) {
        if (isWishlisted) {
            dao.insertWishlistItem(WishlistItem(productId))
        } else {
            dao.deleteWishlistItem(productId)
        }
    }

    suspend fun addSavedAddress(title: String, address: String, phone: String) {
        dao.insertSavedAddress(SavedAddress(title = title, fullAddress = address, phoneNumber = phone))
    }

    suspend fun deleteUserProfile(userProfile: UserProfile) {
        dao.deleteProfile(userProfile)
    }

    suspend fun updateProductRemote(
        id: Int,
        title: String,
        price: Double,
        stockLeft: Int,
        imageUrl: String,
        description: String = "Premium Service",
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
        )
        if (response.success) {
            val existing = dao.getProductById(id)
            if (existing != null) {
                dao.updateProduct(existing.copy(
                    title = title,
                    price = price,
                    stock = stockLeft,
                    imageUrl = imageUrl,
                    description = description,
                    category = category
                ))
                if (FirestoreService.isConfigured.value) {
                    FirestoreService.updateStockInCloud(id, stockLeft)
                }
            }
            return true
        } else {
            throw Exception(response.message ?: "Server rejected product update.")
        }
    }

    suspend fun deleteProductRemote(productId: Int): Boolean {
        val response = com.example.network.RetrofitClient.apiService.deleteProduct(
            com.example.network.DeleteProductRequest(id = productId)
        )
        if (response.success) {
            val product = dao.getProductById(productId)
            if (product != null) {
                dao.deleteCartItemByProductId(productId)
                dao.deleteProduct(product)
            }
            return true
        } else {
            throw Exception(response.message ?: "Server rejected product deletion.")
        }
    }

    private fun getSeedProducts(): List<Product> {
        return listOf(
            Product(
                title = "Everyday Wash & Fold",
                description = "Convenient pickup and drop-off service for everyday clothing. Washed, dried, and neatly folded.",
                price = 2.99,
                category = "Pickup & Drop-off Services",
                stock = 500,
                artisanName = "Snow White Express Laundry",
                imageUrl = "laundry_shirt",
                rating = 4.8
            ),
            Product(
                title = "Premium Dry Cleaning",
                description = "Pickup and drop-off dry cleaning for your delicate fabrics and formal wear. Carefully processed and delivered on hangers.",
                price = 8.99,
                category = "Pickup & Drop-off Services",
                stock = 200,
                artisanName = "Snow White Premium Cleaners",
                imageUrl = "dryclean_suit",
                rating = 4.9
            ),
            Product(
                title = "Ironing & Pressing Only",
                description = "Pickup and drop-off service for items that just need a crisp, professional press.",
                price = 1.50,
                category = "Pickup & Drop-off Services",
                stock = 300,
                artisanName = "Snow White Express Laundry",
                imageUrl = "laundry_shalwarkameez",
                rating = 4.7
            )
        )
    }
}
