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
    val ordersState: StateFlow<List<Order>> = repository.allOrders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Logged-in User Profile state
    val loggedInUser: StateFlow<UserProfile?> = repository.loggedInUser
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
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

    init {
        loadProductsFromApi()
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
                onResult(success, if (success) null else "Invalid username or security combination.")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Sign-in error.")
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
        if (uppercaseCode == "SNOW15") {
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
        cardNumber: String,
        cardHolder: String,
        expiryDate: String,
        cvv: String,
        shippingAddress: String,
        pickupSchedule: String,
        deliverySchedule: String
    ) {
        // Double Check Form entries
        if (shippingAddress.isBlank() || pickupSchedule.isBlank() || deliverySchedule.isBlank()) {
            paymentResultError = "Please complete all fields including pickup and delivery schedules."
            return
        }

        if (paymentMethod != "cod") {
            if (cardNumber.length < 13 || cardHolder.isBlank() || expiryDate.isBlank() || cvv.length < 3) {
                paymentResultError = "Please complete all credit card fields."
                return
            }

            // Apply basic check digits verification logic (Luhn check)
            val isCardValid = validateCardLuhn(cardNumber)
            if (!isCardValid) {
                paymentResultError = "Payment failed: Invalid Credit Card number check (Luhn Algorithm mismatch)."
                return
            }
        }

        viewModelScope.launch {
            isPaymentProcessing = true
            paymentResultError = null
            paymentResultSuccess = null
            
            // Simulate bank gateway latency to depict processing & verification sequence
            delay(1500)

            try {
                val cardLast4 = if (paymentMethod == "cod") "" else cardNumber.takeLast(4)
                val order = repository.checkOutCart(
                    paymentMethod = paymentMethod,
                    cardLast4 = if (paymentMethod == "cod") "" else "Visa *${cardLast4}",
                    shippingAddress = shippingAddress,
                    pickupSchedule = pickupSchedule,
                    deliverySchedule = deliverySchedule
                )
                paymentResultSuccess = order
                clearPromoCode()
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
