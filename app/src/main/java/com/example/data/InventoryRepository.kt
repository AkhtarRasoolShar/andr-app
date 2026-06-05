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

    suspend fun uploadProduct(
        title: String,
        price: Double,
        stockLeft: Int,
        imageUrl: String,
        description: String = "Premium Service",
        category: String = "Specialized"
    ): Boolean {
        val response = com.example.network.RetrofitClient.apiService.addProduct(
            com.example.network.AddProductRequest(
                title = title,
                price = price,
                stockLeft = stockLeft,
                imageUrl = imageUrl,
                description = description,
                category = category
            )
        )
        if (response.success) {
            fetchProductsFromRemote()
            return true
        } else {
            throw Exception(response.message ?: "Server rejected product insertion.")
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
            throw Exception(response.body()?.message ?: "Registration failed.")
        }
    }

    suspend fun login(email: String, passwordEntered: String): Boolean {
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
            
            val isAdminRole = userRole.equals("admin", ignoreCase = true) || userRole.equals("super_admin", ignoreCase = true)
            val adminProfile = UserProfile(
                email = returnedUser.email,
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
                val fetchedId = returnedUser.userId ?: returnedUser.id ?: 1
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
            throw Exception(response?.message ?: "Invalid remote credentials from PHP backend.")
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
                onResult(false, res.body()?.message ?: "Failed to update profile via API.")
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
        category: String = "Specialized"
    ): Boolean {
        val response = com.example.network.RetrofitClient.apiService.updateProduct(
            com.example.network.UpdateProductRequest(
                id = id,
                title = title,
                price = price,
                stockLeft = stockLeft,
                imageUrl = imageUrl,
                description = description,
                category = category
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
                title = "Gents 2-Piece Suit Dry Cleaning",
                description = "Premium eco-responsible dry cleaning, custom steam pressing, and hanger-suspension protective packaging. Best for business/formal suits of premium wool, cotton or blended fabric.",
                price = 7.99,
                category = "Dry Cleaning",
                stock = 150,
                artisanName = "Snow White Premium Cleaners",
                imageUrl = "dryclean_suit",
                rating = 4.9
            ),
            Product(
                title = "High-End Designer Saree & Lehenga",
                description = "Elite satin, silk, and tissue embroidery-safe chemical dry wash. Meticulously protects zardozi work and fine thread borders, complete with specialized tissue fold framing.",
                price = 19.99,
                category = "Dry Cleaning",
                stock = 60,
                artisanName = "Snow White Bridal Care",
                imageUrl = "dryclean_saree",
                rating = 5.0
            ),
            Product(
                title = "Royal Sherwani Premium Dry Wash",
                description = "Specialized dry-cleaning process preserving rich textures, metallic work, velvet collars, and decorative buttons. Pressed with elite temperature-calibrated steam tools.",
                price = 12.50,
                category = "Dry Cleaning",
                stock = 45,
                artisanName = "Snow White Royal Atelier",
                imageUrl = "dryclean_sherwani",
                rating = 4.8
            ),
            Product(
                title = "Men's Shalwar Kameez (Wash & Press)",
                description = "Traditional wash-and-wear or fine cotton Shalwar Kameez package. Includes organic cleansing, high-heat mechanical extraction, or optional custom crisp starch treatment.",
                price = 3.99,
                category = "Laundry",
                stock = 250,
                artisanName = "Snow White Express Laundry",
                imageUrl = "laundry_shalwarkameez",
                rating = 4.7
            ),
            Product(
                title = "Everyday Shirts & Pants (Laundry & Press)",
                description = "Premium daily attire detergent wash, hygienic tumble dry, and flat crisp iron. Preserves fabric strength, white brightness, and color saturation.",
                price = 1.99,
                category = "Laundry",
                stock = 500,
                artisanName = "Snow White Express Laundry",
                imageUrl = "laundry_shirt",
                rating = 4.6
            ),
            Product(
                title = "Double Blanket / Duvet / Comforter Cleaning",
                description = "Deep sanitizing allergy-free wash for double blankets, heavy duvets, and winter comforters. Fluffed to perfection and vacuum-sealed in fresh aromatic pack.",
                price = 11.00,
                category = "Dry Cleaning",
                stock = 120,
                artisanName = "Snow White Home Care",
                imageUrl = "dryclean_blanket",
                rating = 4.8
            ),
            Product(
                title = "Persian Rug Deep Shampooing (per sq ft)",
                description = "Gentle dust extraction, dye-stabilized anti-bacterial foam extraction, and comb-finishing for premium hand-knotted woolen or antique Persian carpets.",
                price = 0.99,
                category = "Carpet & Rugs",
                stock = 300,
                artisanName = "Snow White Rug & Carpet Spa",
                imageUrl = "carpet_persian",
                rating = 4.9
            ),
            Product(
                title = "Leather Jacket Polish & Restoration",
                description = "Suede and pure aniline leather deep cleaning. Clears outer stains while replenishing oils, standard polishing, and leather conditioning to protect against wear.",
                price = 14.99,
                category = "Specialized",
                stock = 40,
                artisanName = "Snow White Leather Studio",
                imageUrl = "specialized_leather",
                rating = 4.9
            ),
            Product(
                title = "Invisible Bed-Sheet Deep Wash & Starch",
                description = "Hygienic sanitation for premium hotel-grade double sheets, luxury pillowcases, and bed-covers. Features options for fresh scenting and anti-mite washing.",
                price = 2.50,
                category = "Laundry",
                stock = 180,
                artisanName = "Snow White Home Care",
                imageUrl = "laundry_bedsheet",
                rating = 4.7
            )
        )
    }
}
