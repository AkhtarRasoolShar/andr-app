package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class LoginRequest(
    val email: String,
    @Json(name = "password") val passwordEntered: String
)

data class UserProfileResponse(
    val email: String,
    val name: String? = null,
    @Json(name = "full_name") val fullNameFallback: String? = null,
    @Json(name = "phone_number") val phoneNumber: String? = null,
    val phone: String? = null,
    val city: String? = null,
    @Json(name = "delivery_address") val deliveryAddress: String? = null,
    val address: String? = null,
    @Json(name = "membership_points") val membershipPoints: Int = 100,
    val role: String = "customer",
    @Json(name = "user_id") val userId: Int? = null,
    @Json(name = "id") val id: Int? = null
)
{
    val fullName: String
        get() = name ?: fullNameFallback ?: ""
        
    val derivedPhone: String
        get() = phoneNumber ?: phone ?: ""
        
    val derivedAddress: String
        get() = deliveryAddress ?: address ?: ""
}

data class LoginResponse(
    val success: Boolean,
    val message: String? = null,
    val role: String? = null,
    val user: UserProfileResponse? = null
)

data class RegisterRequest(
    val email: String,
    val name: String,
    @Json(name = "password") val passwordEntered: String,
    @Json(name = "phone_number") val phoneNumber: String,
    val city: String,
    @Json(name = "delivery_address") val deliveryAddress: String
)

data class RegisterResponse(
    val success: Boolean,
    val message: String? = null
)

data class AddProductRequest(
    val title: String,
    val price: Double,
    @Json(name = "stock_left") val stockLeft: Int,
    @Json(name = "image_url") val imageUrl: String,
    val description: String = "Premium Service",
    val category: String = "Specialized"
)

data class AddProductResponse(
    val success: Boolean,
    val message: String? = null
)

data class UpdateProductRequest(
    val id: Int,
    val title: String,
    val price: Double,
    @Json(name = "stock_left") val stockLeft: Int,
    @Json(name = "image_url") val imageUrl: String,
    val description: String = "Premium Service",
    val category: String = "Specialized"
)

data class UpdateProductResponse(
    val success: Boolean,
    val message: String? = null
)

data class DeleteProductRequest(
    val id: Int
)

data class DeleteProductResponse(
    val success: Boolean,
    val message: String? = null
)

data class UploadImageRequest(
    val image: String // base64-encoded string representation
)

data class UploadImageResponse(
    val success: Boolean,
    @Json(name = "image_url") val imageUrl: String? = null,
    val message: String? = null
)

data class OrderRequest(
    @Json(name = "user_id") val userId: Int,
    @Json(name = "total_amount") val totalAmount: Double,
    @Json(name = "payment_method") val paymentMethod: String = "COD",
    val address: String,
    val phone: String,
    @Json(name = "items") val items: List<NetworkCartItem>
)

data class NetworkCartItem(
    @Json(name = "product_id") val productId: Int,
    val quantity: Int,
    val price: Double
)

data class OrderResponse(
    val success: Boolean,
    val message: String? = null,
    @Json(name = "order_id") val orderId: String? = null
)

data class GuestOrderRequest(
    @Json(name = "tracking_id") val trackingId: String,
    @Json(name = "guest_name") val guestName: String,
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "delivery_address") val deliveryAddress: String,
    @Json(name = "total_amount") val totalAmount: Double,
    @Json(name = "payment_method") val paymentMethod: String
)

data class ApiResponse(
    val status: String?,
    val message: String?
)

data class OrderHistoryResponse(
    val success: Boolean,
    val orders: List<NetworkOrder>?
)

data class NetworkOrder(
    val id: String,
    @Json(name = "total_amount") val totalAmount: Double,
    val status: String,
    @Json(name = "created_at") val createdAt: String
)

data class ProductResponse(
    val id: Int = 0,
    val title: String,
    val price: Double,
    @Json(name = "stock_left") val stockLeft: Int = 0,
    @Json(name = "image_url") val imageUrl: String = "",
    val description: String? = null,
    val category: String? = null,
    val artisanName: String? = null,
    val rating: Double? = null
)

data class ProductListResponse(
    val success: Boolean,
    val products: List<ProductResponse>?
)

data class FcmTokenRequest(
    @Json(name = "user_id") val userId: Int,
    @Json(name = "fcm_token") val fcmToken: String
)

data class FcmTokenResponse(
    val success: Boolean,
    val message: String? = null
)

data class WishlistRequest(
    @Json(name = "user_id") val userId: Int,
    @Json(name = "product_id") val productId: Int
)

data class WishlistToggleResponse(
    val success: Boolean,
    @Json(name = "is_favorite") val isFavorite: Boolean?,
    val message: String? = null
)

data class SettingsResponse(
    val success: Boolean,
    val settings: Map<String, String>? = null,
    val message: String? = null,
    @Json(name = "latest_app_version") val latestAppVersion: String? = null,
    @Json(name = "app_download_link") val appDownloadLink: String? = null
)

data class UpdateProfileRequest(
    @Json(name = "user_id") val userId: Int,
    @Json(name = "full_name") val fullName: String,
    val email: String,
    @Json(name = "phone_number") val phoneNumber: String
)

data class UpdateProfileResponse(
    val success: Boolean,
    val message: String? = null
)

interface SnowwhiteApi {
    @GET("log_visitor.php")
    suspend fun logVisitor(): retrofit2.Response<Unit>

    @POST("login.php")
    suspend fun loginUser(@Body request: LoginRequest): retrofit2.Response<LoginResponse>

    @POST("register.php")
    suspend fun registerUser(@Body request: RegisterRequest): retrofit2.Response<RegisterResponse>

    @POST("update_profile.php")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): retrofit2.Response<UpdateProfileResponse>

    @POST("update_fcm.php")
    suspend fun updateFcmToken(@Body request: FcmTokenRequest): FcmTokenResponse

    @GET("get_products.php")
    suspend fun getLiveProducts(): retrofit2.Response<ProductListResponse>
    
    @POST("sync_guest_order.php")
    suspend fun syncGuestOrder(@Body request: GuestOrderRequest): retrofit2.Response<ApiResponse>

    @POST("add_product.php")
    suspend fun addProduct(@Body request: AddProductRequest): AddProductResponse

    @POST("upload_image.php")
    suspend fun uploadImage(@Body request: UploadImageRequest): UploadImageResponse

    @POST("place_order.php")
    suspend fun placeOrder(@Body request: OrderRequest): retrofit2.Response<OrderResponse>

    @GET("get_orders.php")
    suspend fun getMyOrders(@retrofit2.http.Query("user_id") userId: Int): retrofit2.Response<OrderHistoryResponse>

    @POST("update_product.php")
    suspend fun updateProduct(@Body request: UpdateProductRequest): UpdateProductResponse

    @POST("delete_product.php")
    suspend fun deleteProduct(@Body request: DeleteProductRequest): DeleteProductResponse

    @POST("wishlist.php")
    suspend fun toggleWishlist(@Body request: WishlistRequest): retrofit2.Response<WishlistToggleResponse>

    @GET("wishlist.php")
    suspend fun getMyWishlist(@retrofit2.http.Query("user_id") userId: Int): retrofit2.Response<ProductListResponse>

    @GET("get_settings.php")
    suspend fun getAppSettings(): retrofit2.Response<SettingsResponse>
}

object RetrofitClient {
    private const val BASE_URL = "https://akhtarhussain.site/api/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val securityInterceptor = okhttp3.Interceptor { chain ->
        val original = chain.request()
        val request = original.newBuilder()
            .header("X-API-KEY", "SnowWhite_Secure_Key_2026")
            .method(original.method, original.body)
            .build()
        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(securityInterceptor)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    val apiService: SnowwhiteApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SnowwhiteApi::class.java)
    }
}
