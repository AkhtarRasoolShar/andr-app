package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface ApiProductState {
    object Loading : ApiProductState
    data class Success(val products: List<Product>) : ApiProductState
    data class Error(val message: String) : ApiProductState
}

data class CartUiItem(
    val cartItem: CartItem,
    val product: Product
)

data class CartSummary(
    val items: List<CartUiItem> = emptyList(),
    val subtotal: Double = 0.0,
    val appliedDiscount: Double = 0.0,
    val shippingFee: Double = 5.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val promoCode: String = ""
)

class MarketViewModel(
    application: Application,
    private val repository: InventoryRepository
) : AndroidViewModel(application) {

    // App Settings Toggles
    var pushNotificationsEnabled by mutableStateOf(true)
        private set
    var darkModeEnabled by mutableStateOf(false)
        private set

    // Store Settings Toggles
    var pauseOrdersEnabled by mutableStateOf(false)
        private set
    var storePolicyText by mutableStateOf("Welcome to our Premium Store. Quality guaranteed.")
        private set

    fun updatePushNotificationsEnabled(enabled: Boolean) {
        pushNotificationsEnabled = enabled
    }

    fun updateDarkModeEnabled(enabled: Boolean) {
        darkModeEnabled = enabled
    }

    fun updatePauseOrdersEnabled(enabled: Boolean) {
        pauseOrdersEnabled = enabled
    }

    fun updateStorePolicyText(policy: String) {
        storePolicyText = policy
    }

    // Filter and search criteria
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Products Flow filtered by Category and Search Query
    val productsState: StateFlow<List<Product>> = combine(
        repository.allProducts,
        _selectedCategory,
        _searchQuery
    ) { rawProducts, category, query ->
        rawProducts.filter { product ->
            val matchesCategory = category == "All" || product.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isBlank() || 
                    product.title.contains(query, ignoreCase = true) ||
                    product.description.contains(query, ignoreCase = true) ||
                    product.artisanName.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Coupon administration
    private val _couponCode = MutableStateFlow("")
    val couponCode: StateFlow<String> = _couponCode.asStateFlow()

    private val _couponError = MutableStateFlow<String?>(null)
    val couponError: StateFlow<String?> = _couponError.asStateFlow()

    private val _couponSuccess = MutableStateFlow<String?>(null)
    val couponSuccess: StateFlow<String?> = _couponSuccess.asStateFlow()

    // Cart details combined with product states
    val cartSummary: StateFlow<CartSummary> = combine(
        repository.allCartItems,
        repository.allProducts,
        _couponCode
    ) { cartItems, products, coupon ->
        val itemsList = cartItems.mapNotNull { cartItem ->
            val correspondingProduct = products.find { it.id == cartItem.productId }
            correspondingProduct?.let { CartUiItem(cartItem, it) }
        }

        val subtotal = itemsList.sumOf { it.product.price * it.cartItem.quantity }
        val discountRate = when (coupon.uppercase().trim()) {
            "SNOW10" -> 0.10 // 10% Off
            "SNOW15" -> 0.15 // 15% Off
            "GLOW20" -> 0.20 // 20% Off
            "HANDMADE10" -> 0.10 // Backwards compatibility 10%
            "ARTISAN20" -> 0.20 // Backwards compatibility 20%
            "FREESHIP" -> 0.00 // Handles free shipping below
            else -> 0.00
        }

        var discount = subtotal * discountRate
        var shipping = if (subtotal > 0.0) 3.50 else 0.0 // Reduced shipping for local logistics
        
        if (coupon.uppercase().trim() == "FREESHIP" && subtotal > 0.0) {
            shipping = 0.0
        }

        val tax = subtotal * 0.15 // 15% standard sales tax
        val finalTotal = (subtotal - discount + shipping + tax).coerceAtLeast(0.0)

        CartSummary(
            items = itemsList,
            subtotal = subtotal,
            appliedDiscount = discount,
            shippingFee = shipping,
            tax = tax,
            total = finalTotal,
            promoCode = coupon
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CartSummary()
    )

    // Orders Flow
    private val _ordersState = MutableStateFlow<List<Order>?>(null)
    val ordersState: StateFlow<List<Order>?> = _ordersState.asStateFlow()

    private val _adminAllOrdersState = MutableStateFlow<List<com.example.network.NetworkOrder>?>(null)
    val adminAllOrdersState: StateFlow<List<com.example.network.NetworkOrder>?> = _adminAllOrdersState.asStateFlow()

    fun loadAdminAllOrders() {
        viewModelScope.launch {
            while (true) {
                try {
                    val res = com.example.network.RetrofitClient.apiService.getAllOrders()
                    if (res.isSuccessful && res.body()?.success == true) {
                        _adminAllOrdersState.value = res.body()?.orders
                    }
                } catch (e: Exception) {
                    // silently fail
                }
                kotlinx.coroutines.delay(30000)
            }
        }
    }

    fun loadOrders() {
        val sessionManager = com.example.data.SessionManager(getApplication())
        val session = sessionManager.fetchSession()
        
        // Start collecting local orders continuously so the UI always reflects the database
        viewModelScope.launch {
            repository.allOrders.collect { localList ->
                _ordersState.value = localList
            }
        }

        viewModelScope.launch {
            while (true) {
                try {
                    val sessionManager = com.example.data.SessionManager(getApplication())
                    val session = sessionManager.fetchSession()
                    if (session != null && session.userId > 0) {
                        val response = com.example.network.RetrofitClient.apiService.getMyOrders(session.userId)
                        if (response.isSuccessful && response.body()?.success == true) {
                            val networkOrders = response.body()?.orders ?: emptyList()
                            val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                            
                            // Fetch local orders once for comparison
                            val currentLocalOrders = repository.allOrders.first()
                            val currentMap = currentLocalOrders.associateBy { it.id }

                            networkOrders.forEach { netOrder ->
                                val existing = currentMap[netOrder.id]
                                if (existing != null) {
                                    if (existing.status != netOrder.status) {
                                        repository.saveLocalOrder(existing.copy(status = netOrder.status))
                                    }
                                } else {
                                    val parsedTime = try { format.parse(netOrder.createdAt)?.time ?: System.currentTimeMillis() } catch (e: Exception) { System.currentTimeMillis() }
                                    repository.saveLocalOrder(
                                        com.example.data.Order(
                                            id = netOrder.id,
                                            timestamp = parsedTime,
                                            itemsSummary = "Purchased Items",
                                            totalAmount = netOrder.totalAmount,
                                            status = netOrder.status,
                                            paymentCardLast4 = "API",
                                            shippingAddress = "Delivery Address"
                                        )
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Silently fail network error
                }
                kotlinx.coroutines.delay(10000) // Poll every 10 seconds
            }
        }
    }

    // Logged-in User Profile state
    val loggedInUser: StateFlow<UserProfile?> = repository.loggedInUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val wishlistIds: StateFlow<List<Int>> = repository.wishlistIds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val savedAddresses: StateFlow<List<SavedAddress>> = repository.savedAddresses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleWishlist(productId: Int) {
        val current = wishlistIds.value
        val isWishlisted = current.contains(productId)
        viewModelScope.launch {
            val sessionManager = com.example.data.SessionManager(getApplication())
            val session = sessionManager.fetchSession()
            if (session != null && session.userId > 0) {
                try {
                    val request = com.example.network.WishlistRequest(session.userId, productId)
                    val response = com.example.network.RetrofitClient.apiService.toggleWishlist(request)
                    if (response.isSuccessful && response.body()?.success == true) {
                        val isFavRes = response.body()?.isFavorite
                        if (isFavRes != null) {
                            repository.toggleWishlist(productId, isFavRes)
                            return@launch
                        }
                    }
                } catch (e: Exception) {
                    // fallthrough
                }
            }
            repository.toggleWishlist(productId, !isWishlisted)
        }
    }

    fun loadWishlistFromApi() {
        val sessionManager = com.example.data.SessionManager(getApplication())
        val session = sessionManager.fetchSession()
        if (session != null && session.userId > 0) {
            viewModelScope.launch {
                try {
                    val response = com.example.network.RetrofitClient.apiService.getMyWishlist(session.userId)
                    if (response.isSuccessful && response.body()?.success == true) {
                        val networkProducts = response.body()?.products ?: emptyList()
                        networkProducts.forEach { prod ->
                            repository.toggleWishlist(prod.id, true)
                        }
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    fun addSavedAddress(title: String, address: String, phone: String) {
        viewModelScope.launch {
            repository.addSavedAddress(title, address, phone)
        }
    }

    val allUserProfiles: StateFlow<List<UserProfile>> = repository.allUserProfiles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _apiState = MutableStateFlow<ApiProductState>(ApiProductState.Loading)
    val apiState: StateFlow<ApiProductState> = _apiState.asStateFlow()

    // Payment Sandbox / Checkout states
    var isPaymentProcessing by mutableStateOf(false)
        private set

    var paymentResultSuccess by mutableStateOf<Order?>(null)
        private set

    var paymentResultError by mutableStateOf<String?>(null)
        private set

    var appBannerUrl by mutableStateOf<String?>(null)
        private set

    var appSettingsMap by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    var latestAppVersion by mutableStateOf<String?>(null)
        private set
        
    var appDownloadLink by mutableStateOf<String?>(null)
        private set

    init {
        loadProductsFromApi()
        loadAppSettings()
        loadOrders()
        loadAdminAllOrders()
        
        // Silently log visitor
        viewModelScope.launch {
            try {
                com.example.network.RetrofitClient.apiService.logVisitor()
            } catch (e: Exception) {
                // Ignore network failures for visitor logging
            }
        }
    }

    private fun loadAppSettings() {
        viewModelScope.launch {
            try {
                val res = com.example.network.RetrofitClient.apiService.getAppSettings()
                if (res.isSuccessful && res.body()?.success == true) {
                    val settings = res.body()?.settings ?: emptyMap()
                    appSettingsMap = settings
                    appBannerUrl = settings["app_banner"]
                    
                    // Fallback to settings map if top-level fields are missing but map contains them
                    latestAppVersion = res.body()?.latestAppVersion ?: settings["latest_app_version"]
                    appDownloadLink = res.body()?.appDownloadLink ?: settings["app_download_link"]
                }
            } catch (e: Exception) {
            }
        }
    }

    fun loadProductsFromApi() {
        viewModelScope.launch {
            _apiState.value = ApiProductState.Loading
            try {
                val list = repository.fetchProductsFromRemote()
                _apiState.value = ApiProductState.Success(list)
            } catch (e: Exception) {
                _apiState.value = ApiProductState.Error(e.message ?: "Failed to connect to remote server.")
            }
        }
    }

    // Account Creation / Membership actions
    fun createAccount(
        fullName: String,
        email: String,
        phone: String,
        city: String,
        address: String,
        passwordEntered: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.createAccount(
                    UserProfile(
                        email = email.trim(),
                        fullName = fullName.trim(),
                        phoneNumber = phone.trim(),
                        city = city.trim(),
                        deliveryAddress = address.trim(),
                        membershipPoints = 150 // Welcome loyalty points bonus!
                    ),
                    passwordEntered
                )
                // Force login in background to save session properly
                repository.login(email.trim(), passwordEntered)
                
                fetchAndUploadFcmToken()
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Unknown creation failure.")
            }
        }
    }

    fun login(email: String, passwordEntered: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val success = repository.login(email, passwordEntered)
                if (success) {
                    fetchAndUploadFcmToken()
                }
                onResult(success, if (success) null else "Invalid username or security combination.")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Sign-in error.")
            }
        }
    }

    fun forgotPassword(email: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = com.example.network.RetrofitClient.apiService.forgotPassword(com.example.network.ForgotPasswordRequest(email))
                if (response.isSuccessful && response.body()?.success == true) {
                    onResult(true, response.body()?.message ?: "Password reset instructions sent.")
                } else {
                    val errorMsg = com.example.network.ErrorUtils.parseErrorMessage(response)
                    onResult(false, errorMsg ?: response.body()?.message ?: "Failed to reset password.")
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Network error.")
            }
        }
    }

    private fun fetchAndUploadFcmToken() {
        val sessionMgr = com.example.data.SessionManager(getApplication())
        val userId = sessionMgr.fetchSession()?.userId
        if (userId != null) {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    viewModelScope.launch {
                        try {
                            com.example.network.RetrofitClient.apiService.updateFcmToken(
                                com.example.network.FcmTokenRequest(userId, token)
                            )
                        } catch (e: Exception) {
                            // Ignore network failures for FCM
                        }
                    }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun autoLoginFromCache(email: String, fullName: String, role: String) {
        viewModelScope.launch {
            try {
                repository.ensureAutoLoginUser(email, fullName, role)
            } catch (e: Exception) {
                // Ignore matching mistakes
            }
        }
    }

    fun uploadProduct(
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
                )
                if (success) {
                    loadProductsFromApi() // force refresh
                    onResult(true, null)
                } else {
                    onResult(false, "Unknown upload rejection.")
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Product upload failure.")
            }
        }
    }

    fun uploadImage(base64Image: String, onResult: (String?, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val url = repository.uploadImage(base64Image)
                onResult(url, null)
            } catch (e: Exception) {
                onResult(null, e.localizedMessage ?: "Image upload fail.")
            }
        }
    }

    fun toggleSavedPreference(productId: Int) {
        viewModelScope.launch {
            val user = loggedInUser.value ?: return@launch
            val preferencesList = user.savedPreferences.split(",").filter { it.isNotBlank() }.toMutableList()
            val prodIdStr = productId.toString()
            if (preferencesList.contains(prodIdStr)) {
                preferencesList.remove(prodIdStr)
            } else {
                preferencesList.add(prodIdStr)
            }
            val updatedString = preferencesList.joinToString(",")
            repository.updateSavedPreferences(user.email, updatedString)
        }
    }

    fun updateSavedPreferences(newPreferencesRaw: String) {
        viewModelScope.launch {
            val user = loggedInUser.value ?: return@launch
            repository.updateSavedPreferences(user.email, newPreferencesRaw)
        }
    }

    fun updateUserProfile(userId: Int, oldEmail: String, email: String, fullName: String, phoneNumber: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            repository.updateUserProfile(userId, oldEmail, email, fullName, phoneNumber, onResult)
        }
    }

    // Filter functions
    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Coupon actions
    fun applyPromoCode(code: String) {
        val uppercaseCode = code.uppercase().trim()
        if (uppercaseCode == "SNOW10") {
            _couponCode.value = "SNOW10"
            _couponSuccess.value = "10% Promo Code \"SNOW10\" applied!"
            _couponError.value = null
        } else if (uppercaseCode == "SNOW15") {
            _couponCode.value = "SNOW15"
            _couponSuccess.value = "15% Welcome Promo Code \"SNOW15\" applied!"
            _couponError.value = null
        } else if (uppercaseCode == "GLOW20") {
            _couponCode.value = "GLOW20"
            _couponSuccess.value = "20% Glow VIP Promo Code applied!"
            _couponError.value = null
        } else if (uppercaseCode == "FREESHIP") {
            _couponCode.value = "FREESHIP"
            _couponSuccess.value = "Free Shipping Promo Code applied!"
            _couponError.value = null
        } else if (uppercaseCode == "HANDMADE10" || uppercaseCode == "ARTISAN20") {
            _couponCode.value = uppercaseCode
            _couponSuccess.value = "Promo Code \"$uppercaseCode\" applied successfully!"
            _couponError.value = null
        } else {
            _couponError.value = "Invalid Promo Code."
            _couponSuccess.value = null
        }
    }

    fun clearPromoCode() {
        _couponCode.value = ""
        _couponError.value = null
        _couponSuccess.value = null
    }

    // Card Actions
    fun addToCart(product: Product) {
        viewModelScope.launch {
            repository.addToCart(product.id)
        }
    }

    fun decreaseCartQuantity(productId: Int) {
        viewModelScope.launch {
            repository.decreaseCartItem(productId)
        }
    }

    fun removeFromCart(productId: Int) {
        viewModelScope.launch {
            repository.removeFromCart(productId)
        }
    }

    // Admin Custom Actions on Products (For inventory management / stock checks)
    fun addNewProduct(
        title: String,
        description: String,
        price: Double,
        category: String,
        stock: Int,
        artisanName: String
    ) {
        viewModelScope.launch {
            val imageRef = when (category.lowercase()) {
                "cosmetics" -> "cosmetics_lipstick"
                "fragrances" -> "fragrances_oud"
                "personal care" -> "personal_serum"
                "apparel" -> "apparel_pj"
                "dry cleaning" -> "dry_clean_voucher"
                else -> "cosmetics_lipstick"
            }
            val newProd = Product(
                title = title,
                description = description,
                price = price,
                category = category,
                stock = stock,
                artisanName = artisanName,
                imageUrl = imageRef,
                rating = (45..50).random().toDouble() / 10.0
            )
            repository.insertProduct(newProd)
        }
    }

    fun updateProductDetails(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun updateUserRole(email: String, newRole: String) {
        viewModelScope.launch {
            repository.updateUserRole(email, newRole)
        }
    }

    fun deleteUserProfile(userProfile: UserProfile) {
        viewModelScope.launch {
            repository.deleteUserProfile(userProfile)
        }
    }

    fun updateProductRemote(
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
                )
                if (success) {
                    loadProductsFromApi() // force refresh
                    onResult(true, null)
                } else {
                    onResult(false, "Unknown update rejection.")
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Product update failure.")
            }
        }
    }

    fun manageProductRemote(
        action: String,
        productId: Int? = null,
        title: String? = null,
        price: Double? = null,
        stock: Int? = null,
        imageUrl: String? = null,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val req = com.example.network.ManageProductRequest(action, productId, title, price, stock, imageUrl)
                val response = com.example.network.RetrofitClient.apiService.manageProduct(req)
                if (response.isSuccessful && response.body()?.status == "success") {
                    loadProductsFromApi()
                    onResult(true, response.body()?.message ?: "Success")
                } else {
                    val errorMsg = com.example.network.ErrorUtils.parseErrorMessage(response)
                    onResult(false, errorMsg ?: response.body()?.message ?: "Failed.")
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "API Error")
            }
        }
    }

    fun deleteProductRemote(productId: Int, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val success = repository.deleteProductRemote(productId)
                if (success) {
                    loadProductsFromApi() // force refresh
                    onResult(true, null)
                } else {
                    onResult(false, "Unknown deletion rejection.")
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Product deletion failure.")
            }
        }
    }

    // Checkout / Simulated Payment Gateway
    fun checkout(
        paymentMethod: String,
        shippingAddress: String,
        phone: String,
        fullName: String,
        onSuccess: () -> Unit
    ) {
        val currentSummary = cartSummary.value

        val sessionManager = com.example.data.SessionManager(getApplication())
        val session = sessionManager.fetchSession()
        val userId = session?.userId ?: 0

        // Double Check Form entries
        if (shippingAddress.isBlank() || phone.isBlank() || fullName.isBlank()) {
            paymentResultError = "Please complete all fields"
            return
        }

        viewModelScope.launch {
            isPaymentProcessing = true
            paymentResultError = null
            paymentResultSuccess = null

            try {
                // Construct the network items from cart
                val networkItems = currentSummary.items.map {
                    com.example.network.NetworkCartItem(
                        productId = it.product.id,
                        quantity = it.cartItem.quantity,
                        price = it.product.price
                    )
                }

                val request = com.example.network.OrderRequest(
                    userId = userId,
                    totalAmount = currentSummary.total,
                    paymentMethod = paymentMethod,
                    address = shippingAddress,
                    phone = phone,
                    guestName = if (userId == 0) fullName else null,
                    items = networkItems
                )

                val response = com.example.network.RetrofitClient.apiService.placeOrder(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val orderId = response.body()?.orderId ?: "SNOW-${System.currentTimeMillis()}"
                    val newOrder = com.example.data.Order(
                        id = orderId,
                        timestamp = System.currentTimeMillis(),
                        itemsSummary = "Order from Checkout",
                        totalAmount = currentSummary.total,
                        status = "pending",
                        paymentCardLast4 = paymentMethod,
                        shippingAddress = shippingAddress
                    )
                    paymentResultSuccess = newOrder
                    
                    // Insert into local DB for tracking
                    repository.saveLocalOrder(newOrder)
                    
                    if (userId <= 0) {
                        try {
                            val guestReq = com.example.network.GuestOrderRequest(
                                trackingId = orderId,
                                guestName = fullName,
                                phoneNumber = phone,
                                deliveryAddress = shippingAddress,
                                totalAmount = currentSummary.total,
                                paymentMethod = paymentMethod
                            )
                            com.example.network.RetrofitClient.apiService.syncGuestOrder(guestReq)
                        } catch (e: Exception) {
                            // Silently ignore sync failures for guest orders
                        }
                    }
                    
                    repository.clearCart() 
                    onSuccess()
                } else {
                    val errorMsg = com.example.network.ErrorUtils.parseErrorMessage(response)
                    paymentResultError = errorMsg ?: response.body()?.message ?: "Failed to place order (HTTP ${response.code()})."
                }
            } catch (e: Exception) {
                paymentResultError = e.message ?: "Transaction failed. Please try again."
            } finally {
                isPaymentProcessing = false
            }
        }
    }

    fun acknowledgePaymentResult() {
        paymentResultSuccess = null
        paymentResultError = null
    }

    // --- Admin Master API Requests ---
    suspend fun sendAdminCommand(request: com.example.network.AdminMasterRequest): com.example.network.ApiResponse? {
        return try {
            val response = com.example.network.RetrofitClient.apiService.sendAdminCommand(request)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }

    // Luhn card logic
    internal fun validateCardLuhn(number: String): Boolean {
        val cleaned = number.replace(Regex("\\s+"), "")
        if (cleaned.length < 12 || cleaned.any { !it.isDigit() }) return false
        
        var sum = 0
        var alternate = false
        for (i in cleaned.length - 1 downTo 0) {
            var n = cleaned[i] - '0'
            if (alternate) {
                n *= 2
                if (n > 9) {
                    n = (n % 10) + 1
                }
            }
            sum += n
            alternate = !alternate
        }
        return sum % 10 == 0
    }
}

class MarketViewModelFactory(
    private val application: Application,
    private val repository: InventoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarketViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MarketViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
