package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val price: Double,
    val category: String, // e.g., "Dry Cleaning", "Laundry", "Carpet & Rugs", "Specialized"
    val stock: Int,       // Real-time inventory
    val artisanName: String, // Brand or Service provider
    val imageUrl: String,  // Key for local icon or visual illustration reference
    val rating: Double = 4.8
)

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val quantity: Int
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey val id: String, // e.g., "ORD-5489"
    val timestamp: Long,
    val itemsSummary: String, // e.g., "Matte Lipstick x1, Oud Parfum x1"
    val totalAmount: Double,
    val status: String,       // "Processing", "Shipped", "Delivered"
    val paymentCardLast4: String,
    val shippingAddress: String,
    val pickupSchedule: String = "",
    val deliverySchedule: String = ""
)

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val email: String,
    val fullName: String,
    val phoneNumber: String,
    val city: String,
    val deliveryAddress: String,
    val membershipPoints: Int = 100,
    val isLoggedIn: Boolean = false,
    val savedPreferences: String = "", // Comma-separated list of product IDs or category names
    val purchaseHistory: String = "",   // Comma-separated record of completed transactions/orders
    val isAdmin: Boolean = false,
    val role: String = "customer"
)

@Entity(tableName = "wishlist")
data class WishlistItem(
    @PrimaryKey val productId: Int
)

@Entity(tableName = "saved_addresses")
data class SavedAddress(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val fullAddress: String,
    val phoneNumber: String
)

@Dao
interface MarketplaceDao {
    // Products
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProductsFlow(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: Int): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("UPDATE products SET stock = :newStock WHERE id = :productId")
    suspend fun updateProductStock(productId: Int, newStock: Int)

    // Cart Items
    @Query("SELECT * FROM cart_items")
    fun getCartItemsFlow(): Flow<List<CartItem>>

    @Query("SELECT * FROM cart_items WHERE productId = :productId LIMIT 1")
    suspend fun getCartItemByProductId(productId: Int): CartItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItem)

    @Update
    suspend fun updateCartItem(cartItem: CartItem)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun deleteCartItemByProductId(productId: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    // Orders
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrdersFlow(): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    // User Profiles
    @Query("SELECT * FROM user_profiles WHERE isLoggedIn = 1 LIMIT 1")
    fun getLoggedInUserFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Query("UPDATE user_profiles SET isLoggedIn = 0")
    suspend fun logoutAllUsers()

    @Query("UPDATE user_profiles SET isLoggedIn = 1 WHERE email = :email")
    suspend fun loginUser(email: String)

    @Query("SELECT * FROM user_profiles ORDER BY email ASC")
    fun getAllUserProfilesFlow(): Flow<List<UserProfile>>

    @Query("UPDATE user_profiles SET role = :newRole, isAdmin = :isAdmin WHERE email = :email")
    suspend fun updateUserRole(email: String, newRole: String, isAdmin: Boolean)

    @Delete
    suspend fun deleteProfile(profile: UserProfile)

    // Wishlist
    @Query("SELECT productId FROM wishlist")
    fun getWishlistFlow(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWishlistItem(item: WishlistItem)

    @Query("DELETE FROM wishlist WHERE productId = :productId")
    suspend fun deleteWishlistItem(productId: Int)

    // Addresses
    @Query("SELECT * FROM saved_addresses")
    fun getSavedAddressesFlow(): Flow<List<SavedAddress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedAddress(address: SavedAddress)
}
