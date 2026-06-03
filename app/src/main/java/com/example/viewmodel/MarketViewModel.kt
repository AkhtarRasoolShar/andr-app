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
            "HANDMADE10" -> 0.10 // 10% Off
            "ARTISAN20" -> 0.20 // 20% Off
            "FREESHIP" -> 0.00 // Handles free shipping below
            else -> 0.00
        }

        var discount = subtotal * discountRate
        var shipping = if (subtotal > 0.0) 5.99 else 0.0
        
        if (coupon.uppercase().trim() == "FREESHIP" && subtotal > 0.0) {
            shipping = 0.0
        }

        val tax = subtotal * 0.08 // 8% local craftsman tax
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

    // Payment Sandbox / Checkout states
    var isPaymentProcessing by mutableStateOf(false)
        private set

    var paymentResultSuccess by mutableStateOf<Order?>(null)
        private set

    var paymentResultError by mutableStateOf<String?>(null)
        private set

    init {
        // Initialize Database with handcrafted items if empty
        viewModelScope.launch {
            repository.ensureSeededData()
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
        if (uppercaseCode == "HANDMADE10") {
            _couponCode.value = "HANDMADE10"
            _couponSuccess.value = "10% Promo Code \"HANDMADE10\" applied!"
            _couponError.value = null
        } else if (uppercaseCode == "ARTISAN20") {
            _couponCode.value = "ARTISAN20"
            _couponSuccess.value = "20% Artisan Promo Code applied!"
            _couponError.value = null
        } else if (uppercaseCode == "FREESHIP") {
            _couponCode.value = "FREESHIP"
            _couponSuccess.value = "Free Shipping Promo Code applied!"
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

    // Admin Custom Actions on Products
    fun addNewProduct(
        title: String,
        description: String,
        price: Double,
        category: String,
        stock: Int,
        artisanName: String
    ) {
        viewModelScope.launch {
            // Select icon code based on category
            val imageRef = when (category.lowercase()) {
                "ceramics" -> "ceramics"
                "textiles" -> "textiles"
                "jewelry" -> "jewelry"
                "woodwork" -> "woodwork"
                else -> "ceramics"
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

    // Checkout / Simulated Payment Gateway containing real Luhn's check verification and secure processing loading indicator.
    fun checkout(
        cardNumber: String,
        cardHolder: String,
        expiryDate: String,
        cvv: String,
        shippingAddress: String
    ) {
        // Double Check Form entries
        if (cardNumber.length < 13 || cardHolder.isBlank() || expiryDate.isBlank() || cvv.length < 3 || shippingAddress.isBlank()) {
            paymentResultError = "Please complete all payment and shipping fields correctly."
            return
        }

        // Apply basic check digits verification logic for simulated sandbox card verification
        val isCardValid = validateCardLuhn(cardNumber)
        if (!isCardValid) {
            paymentResultError = "Payment failed: Invalid Credit Card number check (Luhn Algorithm mismatch)."
            return
        }

        viewModelScope.launch {
            isPaymentProcessing = true
            paymentResultError = null
            paymentResultSuccess = null
            
            // Simulate bank gateway latency to depict processing & real-time verification sequence
            delay(2200)

            try {
                // Set the payment card last four characters
                val cardLast4 = cardNumber.takeLast(4)
                val order = repository.checkOutCart(
                    cardLast4 = "Visa *${cardLast4}",
                    shippingAddress = shippingAddress
                )
                paymentResultSuccess = order
                // Reset cart configurations
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

    // Simple helper checking card number correctness via Luhn's formula
    internal fun validateCardLuhn(number: String): Boolean {
        // Strip any spaces or non-digit signs
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
