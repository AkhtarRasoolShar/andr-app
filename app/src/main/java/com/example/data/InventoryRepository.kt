package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class InventoryRepository(private val dao: MarketplaceDao) {

    val allProducts: Flow<List<Product>> = dao.getAllProductsFlow()
    val allCartItems: Flow<List<CartItem>> = dao.getCartItemsFlow()
    val allOrders: Flow<List<Order>> = dao.getAllOrdersFlow()
    val loggedInUser: Flow<UserProfile?> = dao.getLoggedInUserFlow()

    suspend fun createAccount(profile: UserProfile, passwordEntered: String): Boolean {
        if (passwordEntered.trim().length < 6) {
            throw Exception("Password must be at least 6 characters.")
        }
        
        if (FirebaseAuthService.isConfigured.value) {
            try {
                FirebaseAuthService.createUserWithFirebase(profile.email, passwordEntered)
            } catch (e: Exception) {
                throw Exception("Secure Registration failed: ${e.message}")
            }
        }
        
        dao.logoutAllUsers()
        dao.insertProfile(profile.copy(isLoggedIn = true))
        return true
    }

    suspend fun login(email: String, passwordEntered: String): Boolean {
        if (passwordEntered.trim().length < 6) {
            throw Exception("Password must be at least 6 characters.")
        }

        if (FirebaseAuthService.isConfigured.value) {
            try {
                FirebaseAuthService.signInWithFirebase(email, passwordEntered)
            } catch (e: Exception) {
                throw Exception("Secure Sign-In failed: ${e.message}")
            }
        }

        val existing = dao.getUserByEmail(email.trim())
        return if (existing != null) {
            dao.logoutAllUsers()
            dao.loginUser(email.trim())
            true
        } else {
            if (FirebaseAuthService.isConfigured.value) {
                // If account exists in Cloud Auth but local cache cleared, restore/provision local profile gracefully
                val restoredProfile = UserProfile(
                    email = email.trim(),
                    fullName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    phoneNumber = "",
                    city = "",
                    deliveryAddress = "",
                    membershipPoints = 120, // Welcome loyalist package
                    isLoggedIn = true
                )
                dao.logoutAllUsers()
                dao.insertProfile(restoredProfile)
                true
            } else {
                throw Exception("Profile does not exist. Please establish a new account first.")
            }
        }
    }

    suspend fun logout() {
        if (FirebaseAuthService.isConfigured.value) {
            FirebaseAuthService.signOutFirebase()
        }
        dao.logoutAllUsers()
    }

    suspend fun updateSavedPreferences(email: String, preferencesRaw: String) {
        val user = dao.getUserByEmail(email)
        if (user != null) {
            dao.insertProfile(user.copy(savedPreferences = preferencesRaw))
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
        cardLast4: String,
        shippingAddress: String
    ): Order {
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
        val total = cartUiList.sumOf { it.product.price * it.cartItem.quantity }

        if (FirestoreService.isConfigured.value) {
            // Apply Cloud Firestore Multi-Document Transaction to ensure concurrent stock integrity
            try {
                FirestoreService.performFirestoreCheckoutTransaction(cartUiList)
                // Reflect locally immediately for ultra-fast instant UI responsiveness
                for (item in cartUiList) {
                    val updatedStock = item.product.stock - item.cartItem.quantity
                    dao.updateProductStock(item.product.id, updatedStock)
                }
            } catch (e: Exception) {
                throw Exception("Transaction Aborted by Cloud Controller: ${e.message}")
            }
        } else {
            // High-Performance Local transactional validation fallback
            for (item in cartUiList) {
                if (item.product.stock < item.cartItem.quantity) {
                    throw Exception("Insufficient stock for '${item.product.title}'. Only ${item.product.stock} available.")
                }
            }
            // Decrement cached table stocks
            for (item in cartUiList) {
                val updatedStock = item.product.stock - item.cartItem.quantity
                dao.updateProductStock(item.product.id, updatedStock)
            }
        }

        val orderId = "SNOW-${(10000..99999).random()}"
        val order = Order(
            id = orderId,
            timestamp = System.currentTimeMillis(),
            itemsSummary = summaryItemsList.joinToString(", "),
            totalAmount = total,
            status = "Processing",
            paymentCardLast4 = cardLast4,
            shippingAddress = shippingAddress
        )

        // Deduct/Add Loyalty Membership Points if logged in and record details
        val user = loggedInUser.first()
        if (user != null) {
            val pointsEarned = (total * 0.1).toInt().coerceAtLeast(1)
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
