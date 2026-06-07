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

    val appCategories = repository.allCategories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addCategory(name: String, iconName: String = "Star") {
        viewModelScope.launch {
            repository.addCategoryLocal(name, iconName)
        }
    }

    fun deleteCategory(name: String) {
        viewModelScope.launch {
            repository.deleteCategoryLocal(name)
        }
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

    private var previousActiveChatsStateHash = -1

    fun loadAdminAllOrders() {
        viewModelScope.launch {
            var previousOrderCount = -1
            while (true) {
                try {
                    val res = com.example.network.RetrofitClient.apiService.getAllOrders()
                    if (res.isSuccessful && res.body()?.success == true) {
                        val currentOrders = res.body()?.orders ?: emptyList()
                        _adminAllOrdersState.value = currentOrders
                        
                        if (previousOrderCount != -1 && currentOrders.size > previousOrderCount) {
                            com.example.utils.NotificationHelper.sendNotification(getApplication(), "New Order \uD83D\uDCE6", "A new order has been placed by a customer.")
                            com.example.utils.SoundHelper.playChatSound(getApplication())
                        }
                        previousOrderCount = currentOrders.size
                    }
                    
                    // Admin Active Chats Polling for generic notification
                    val sessionManager = com.example.data.SessionManager(getApplication())
                    val role = sessionManager.fetchSession()?.role
                    if (role == "admin" || activeChatUserId != null) {
                        val chatRes = com.example.network.RetrofitClient.apiService.getActiveChats()
                        if (chatRes.isSuccessful && chatRes.body()?.success == true) {
                            val currentChats = chatRes.body()?.chats ?: emptyList()
                            val currentStateHash = currentChats.hashCode()
                            
                            if (previousActiveChatsStateHash != -1 && currentStateHash != previousActiveChatsStateHash) {
                                com.example.utils.NotificationHelper.sendChatNotification(getApplication(), "New Chat Message \uD83D\uDCAC", "A customer has sent a new message.")
                                com.example.utils.SoundHelper.playChatSound(getApplication())
                            }
                            previousActiveChatsStateHash = currentStateHash
                        }
                    }

                } catch (e: Exception) {
                    // silently fail
                }
                kotlinx.coroutines.delay(10000) // Polling more frequently for chat/orders push
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
                                        com.example.utils.NotificationHelper.sendNotification(
                                            getApplication(),
                                            "Order Update",
                                            "Your order #${netOrder.id} status is now: ${netOrder.status.replaceFirstChar { it.uppercase() }}"
                                        )
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

    var activeChatUserId: Int? = null

    var isLiveWithAdmin by mutableStateOf(false)
        private set

    fun updateLiveWithAdmin(enabled: Boolean) {
        val oldState = isLiveWithAdmin
        isLiveWithAdmin = enabled
        getApplication<Application>().getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
            .edit().putBoolean("is_live_with_admin", enabled).apply()
        if (enabled && !oldState) {
            startCustomerChatGlobally()
        } else if (!enabled) {
            customerChatPollingJob?.cancel()
            customerChatPollingJob = null
        }
    }

    private var previousCustomerChatCount = -1
    private var customerChatPollingJob: kotlinx.coroutines.Job? = null

    private fun startCustomerChatGlobally() {
        customerChatPollingJob?.cancel()
        customerChatPollingJob = viewModelScope.launch {
            while (isLiveWithAdmin) {
                try {
                    val user = loggedInUser.value
                    if (user != null && user.id > 0) {
                        val apiMsgs = getChatHistory(user.id, 1) // admin id = 1
                        if (previousCustomerChatCount != -1 && apiMsgs.size > previousCustomerChatCount) {
                            val newMsgs = apiMsgs.subList(previousCustomerChatCount, apiMsgs.size)
                            val latest = newMsgs.last()
                            if (latest.senderId != user.id) { // not sent by user
                                com.example.utils.NotificationHelper.sendChatNotification(
                                    getApplication(), "Support Update", latest.message
                                )
                                com.example.utils.SoundHelper.playChatSound(getApplication())
                            }
                        }
                        previousCustomerChatCount = apiMsgs.size
                    }
                } catch (e: Exception) {}
                kotlinx.coroutines.delay(3000)
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
        val sharedPrefs = getApplication<Application>().getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
        isLiveWithAdmin = sharedPrefs.getBoolean("is_live_with_admin", false)
        if (isLiveWithAdmin) {
            startCustomerChatGlobally()
        }

        loadProductsFromApi()
        startProductPolling()
        loadAppSettings()
        loadOrders()
        loadAdminAllOrders()
        
        viewModelScope.launch {
            val cats = repository.allCategories.first()
            if (cats.isEmpty()) {
                listOf("Dry Cleaning", "Laundry", "Carpet & Rugs", "Specialized").forEach {
                    repository.addCategoryLocal(it)
                }
            }
        }

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

    private var previousProductCount = -1

    private fun startProductPolling() {
        viewModelScope.launch {
            while (true) {
                try {
                    val list = repository.fetchProductsFromRemote()
                    if (previousProductCount != -1 && list.size > previousProductCount) {
                        com.example.utils.NotificationHelper.sendNotification(
                            getApplication(),
                            "New Arrival!",
                            "A new active product has been added to the store."
                        )
                    }
                    previousProductCount = list.size
                    _apiState.value = ApiProductState.Success(list)
                } catch (e: Exception) {
                    // silently fail on polling
                }
                kotlinx.coroutines.delay(60000) // check every 1 minute
            }
        }
    }

    fun loadProductsFromApi() {
        viewModelScope.launch {
            _apiState.value = ApiProductState.Loading
            try {
                val list = repository.fetchProductsFromRemote()
                previousProductCount = list.size
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

    suspend fun getActiveChats(): List<com.example.network.ActiveChatUser> {
        return try {
            val response = com.example.network.RetrofitClient.apiService.getActiveChats("get_conversations")
            if (response.isSuccessful) {
                response.body()?.chats ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getChatHistory(userId: Int, otherId: Int): List<com.example.network.NetworkChatMessage> {
        return repository.fetchChatHistory(userId, otherId)
    }

    suspend fun sendChatMessage(senderId: Int, receiverId: Int, message: String): Boolean {
        return repository.sendChatMessage(senderId, receiverId, message)
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
        description: String? = null,
        category: String? = null,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val req = com.example.network.ManageProductRequest(action, productId, title, price, stock, imageUrl, description, category)
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
        if (userId <= 0) {
            paymentResultError = "Please login to place an order"
            return
        }

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

                var isRemoteSuccess = false
                var orderIdFromApi: String? = null
                
                try {
                    val response = com.example.network.RetrofitClient.apiService.placeOrder(request)
                    if (response.isSuccessful && response.body()?.success == true) {
                        isRemoteSuccess = true
                        orderIdFromApi = response.body()?.orderId
                    }
                } catch (e: Exception) {
                    // Network error, will fallback to local
                }

                // Proceed with local creation if API failed, so user doesn't get blocked
                val orderId = orderIdFromApi ?: "SNOW-${System.currentTimeMillis()}"
                val itemDetails = currentSummary.items.joinToString(separator = "\n") { 
                    "${it.cartItem.quantity}x ${it.product.title}" 
                }
                
                val newOrder = com.example.data.Order(
                    id = orderId,
                    timestamp = System.currentTimeMillis(),
                    itemsSummary = itemDetails.ifBlank { "Order from Checkout" },
                    totalAmount = currentSummary.total,
                    status = "Placed",
                    paymentCardLast4 = paymentMethod,
                    shippingAddress = shippingAddress
                )
                
                paymentResultSuccess = newOrder
                repository.saveLocalOrder(newOrder)
                repository.clearCart() 
                onSuccess()
                isPaymentProcessing = false
            } catch (e: Exception) {
                paymentResultError = e.message ?: "Transaction failed locally."
                isPaymentProcessing = false
            }
        }
    }

    fun acknowledgePaymentResult() {
        paymentResultSuccess = null
        paymentResultError = null
    }

    // --- Admin Master API Requests ---
    suspend fun sendAdminCommandString(request: com.example.network.AdminMasterRequest): String? {
        return try {
            val response = com.example.network.RetrofitClient.apiService.sendAdminCommand(request)
            if (response.isSuccessful) response.body()?.string() else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun fetchAdminUsers(): List<com.example.ui.screens.AdminUser> {
        val request = com.example.network.AdminMasterRequest(action = "get_all_users")
        val jsonString = sendAdminCommandString(request)
        val list = mutableListOf<com.example.ui.screens.AdminUser>()
        try {
            if (jsonString != null) {
                val arrayStr = jsonString.trim()
                val jsonArr = if (arrayStr.startsWith("[")) {
                    org.json.JSONArray(arrayStr)
                } else {
                    val root = org.json.JSONObject(arrayStr)
                    root.optJSONArray("users") ?: root.optJSONArray("data") ?: org.json.JSONArray("[]")
                }
                for (i in 0 until jsonArr.length()) {
                    val obj = jsonArr.getJSONObject(i)
                    val id = obj.optInt("id", obj.optInt("user_id", -1))
                    val email = obj.optString("email", "")
                    val name = obj.optString("name", obj.optString("full_name", ""))
                    val role = obj.optString("role", "customer")
                    if (id != -1 && email.isNotEmpty()) {
                        list.add(com.example.ui.screens.AdminUser(id, name, email, role))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
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
