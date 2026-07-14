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
    @field:Json(name = "password") val passwordEntered: String
)

data class UserProfileResponse(
    val email: String,
    val name: String? = null,
    @field:Json(name = "full_name") val fullNameFallback: String? = null,
    @field:Json(name = "phone_number") val phoneNumber: String? = null,
    val phone: String? = null,
    val city: String? = null,
    @field:Json(name = "delivery_address") val deliveryAddress: String? = null,
    val address: String? = null,
    @field:Json(name = "membership_points") val membershipPoints: Int = 100,
    val role: String = "customer",
    @field:Json(name = "user_id") val userId: Int? = null,
    @field:Json(name = "id") val id: Int? = null
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
    @field:Json(name = "password") val passwordEntered: String,
    @field:Json(name = "phone_number") val phoneNumber: String,
    val city: String,
    @field:Json(name = "delivery_address") val deliveryAddress: String
)

data class RegisterResponse(
    val success: Boolean,
    val message: String? = null
)

data class AddProductRequest(
    val title: String,
    val price: Double,
    @field:Json(name = "stock_left") val stockLeft: Int,
    @field:Json(name = "image_url") val imageUrl: String,
    val description: String = "Premium Service",
    val category: String = "Specialized",
    @field:Json(name = "sub_category") val subCategory: String? = null
)

data class AddProductResponse(
    val success: Boolean,
    val message: String? = null
)

data class ReviewRequest(
    @field:Json(name = "order_id") val orderId: String,
    @field:Json(name = "product_id") val productId: Int,
    val rating: Int,
    val comment: String
)

data class ReviewResponse(
    val success: Boolean,
    val message: String? = null
)

data class UpdateProductRequest(
    val id: Int,
    val title: String,
    val price: Double,
    @field:Json(name = "stock_left") val stockLeft: Int,
    @field:Json(name = "image_url") val imageUrl: String,
    val description: String = "Premium Service",
    val category: String = "Specialized",
    @field:Json(name = "sub_category") val subCategory: String? = null
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
    @field:Json(name = "image_url") val imageUrl: String? = null,
    val message: String? = null
)

data class OrderRequest(
    @field:Json(name = "user_id") val userId: Int,
    @field:Json(name = "total_amount") val totalAmount: Double,
    @field:Json(name = "payment_method") val paymentMethod: String = "COD",
    val address: String,
    val phone: String,
    @field:Json(name = "guest_name") val guestName: String? = null,
    @field:Json(name = "pickup_schedule") val pickupSchedule: String? = null,
    @field:Json(name = "delivery_schedule") val deliverySchedule: String? = null,
    @field:Json(name = "items") val items: List<NetworkCartItem>
)

data class ForgotPasswordRequest(
    val email: String
)

data class ForgotPasswordResponse(
    val success: Boolean,
    val message: String? = null
)

data class NetworkCartItem(
    @field:Json(name = "product_id") val productId: Int,
    val quantity: Int,
    val price: Double
)

data class OrderResponse(
    val success: Boolean,
    val message: String? = null,
    @field:Json(name = "order_id") val orderId: String? = null
)

data class GuestOrderRequest(
    @field:Json(name = "tracking_id") val trackingId: String,
    @field:Json(name = "guest_name") val guestName: String,
    @field:Json(name = "phone_number") val phoneNumber: String,
    @field:Json(name = "delivery_address") val deliveryAddress: String,
    @field:Json(name = "total_amount") val totalAmount: Double,
    @field:Json(name = "payment_method") val paymentMethod: String
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
    @field:Json(name = "total_amount") val totalAmount: Double,
    val status: String,
    @field:Json(name = "created_at") val createdAt: String,
    @field:Json(name = "customer_name") val customerName: String? = null,
    val phone: String? = null,
    @field:Json(name = "delivery_address") val deliveryAddress: String? = null,
    @field:Json(name = "pickup_schedule") val pickupSchedule: String? = null,
    @field:Json(name = "delivery_schedule") val deliverySchedule: String? = null,
    @field:Json(name = "payment_method") val paymentMethod: String? = null,
    val items: List<NetworkOrderItem>? = null
)

data class NetworkOrderItem(
    val id: Int? = null,
    val name: String? = null,
    val quantity: Int? = null,
    val price: Double? = null
)

data class ProductResponse(
    val id: Int = 0,
    val title: String,
    val price: Double,
    @field:Json(name = "stock_left") val stockLeft: Int = 0,
    @field:Json(name = "image_url") val imageUrl: String = "",
    val description: String? = null,
    val category: String? = null,
    @field:Json(name = "sub_category") val subCategory: String? = null,
    val artisanName: String? = null,
    val rating: Double? = null
)

data class ProductListResponse(
    val success: Boolean,
    val products: List<ProductResponse>?
)

data class FcmTokenRequest(
    @field:Json(name = "user_id") val userId: Int,
    @field:Json(name = "fcm_token") val fcmToken: String
)

data class FcmTokenResponse(
    val success: Boolean,
    val message: String? = null
)

data class WishlistRequest(
    @field:Json(name = "user_id") val userId: Int,
    @field:Json(name = "product_id") val productId: Int
)

data class WishlistToggleResponse(
    val success: Boolean,
    @field:Json(name = "is_favorite") val isFavorite: Boolean?,
    val message: String? = null
)

data class SettingsResponse(
    val success: Boolean,
    val settings: Map<String, String>? = null,
    val message: String? = null,
    @field:Json(name = "latest_app_version") val latestAppVersion: String? = null,
    @field:Json(name = "app_download_link") val appDownloadLink: String? = null
)

data class UpdateProfileRequest(
    @field:Json(name = "user_id") val userId: Int,
    @field:Json(name = "full_name") val fullName: String,
    val email: String,
    @field:Json(name = "phone_number") val phoneNumber: String
)

data class UpdateProfileResponse(
    val success: Boolean,
    val message: String? = null
)

data class ManageProductRequest(
    @field:Json(name = "action") val action: String,
    @field:Json(name = "product_id") val productId: Int? = null,
    val title: String? = null,
    val price: Double? = null,
    val stock: Int? = null,
    @field:Json(name = "image_url") val imageUrl: String? = null,
    val description: String? = null,
    val category: String? = null,
    @field:Json(name = "sub_category") val subCategory: String? = null
)

data class NetworkChatMessage(
    val id: Int? = null,
    @field:Json(name = "sender_id") val senderId: Int,
    @field:Json(name = "receiver_id") val receiverId: Int,
    val message: String,
    @field:Json(name = "created_at") val createdAt: String? = null
)

data class ChatHistoryResponse(
    val success: Boolean,
    val messages: List<NetworkChatMessage> = emptyList(),
    val error: String? = null
)

data class ChatSendRequest(
    @field:Json(name = "sender_id") val senderId: Int,
    @field:Json(name = "receiver_id") val receiverId: Int,
    val message: String
)

data class ChatSendResponse(
    val success: Boolean,
    val message: String? = null
)

data class AdminMasterRequest(
    @field:Json(name = "action") val action: String,
    @field:Json(name = "order_id") val orderId: Int? = null,
    @field:Json(name = "status") val status: String? = null,
    @field:Json(name = "user_id") val userId: Int? = null,
    @field:Json(name = "role") val role: String? = null,
    @field:Json(name = "maintenance_mode") val maintenanceMode: Boolean? = null,
    @field:Json(name = "cod_enabled") val codEnabled: Boolean? = null,
    @field:Json(name = "primary_color") val primaryColor: String? = null,
    @field:Json(name = "delivery_fee") val deliveryFee: Double? = null,
    @field:Json(name = "app_name") val appName: String? = null,
    @field:Json(name = "logo_url") val logoUrl: String? = null
)

data class ActiveChatUser(
    @field:Json(name = "id") val userId: Int,
    val name: String,
    val email: String,
    @field:Json(name = "last_message") val lastMessage: String? = null,
    @field:Json(name = "last_message_time") val lastMessageTime: String? = null,
    @field:Json(name = "unread_count") val unreadCount: Int? = null
)

data class ActiveChatsResponse(
    val success: Boolean,
    @field:Json(name = "conversations") val chats: List<ActiveChatUser> = emptyList(),
    val error: String? = null
)

interface SnowwhiteApi {
    @GET("api_chat.php")
    suspend fun getChatHistory(@retrofit2.http.Query("action") action: String = "get_messages", @retrofit2.http.Query("user_id") userId: Int, @retrofit2.http.Query("other_id") otherId: Int): retrofit2.Response<ChatHistoryResponse>
    
    @GET("api_chat.php")
    suspend fun getActiveChats(@retrofit2.http.Query("action") action: String = "get_active_chats"): retrofit2.Response<ActiveChatsResponse>

    @POST("api_chat.php?action=send_message")
    suspend fun sendChatMessage(@Body request: ChatSendRequest): retrofit2.Response<ChatSendResponse>

    @GET("log_visitor.php")
    suspend fun logVisitor(): retrofit2.Response<Unit>

    @POST("login.php")
    suspend fun loginUser(@Body request: LoginRequest): retrofit2.Response<LoginResponse>

    @POST("forgot_password.php")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): retrofit2.Response<ForgotPasswordResponse>

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

    @GET("get_orders.php")
    suspend fun getOrderDetail(@retrofit2.http.Query("order_id") orderId: Int): retrofit2.Response<OrderHistoryResponse>

    @GET("get_orders.php")
    suspend fun getAllOrders(): retrofit2.Response<OrderHistoryResponse>

    @POST("update_product.php")
    suspend fun updateProduct(@Body request: UpdateProductRequest): UpdateProductResponse

    @POST("submit_review.php")
    suspend fun submitReview(@Body request: ReviewRequest): retrofit2.Response<ReviewResponse>

    @POST("delete_product.php")
    suspend fun deleteProduct(@Body request: DeleteProductRequest): DeleteProductResponse

    @POST("wishlist.php")
    suspend fun toggleWishlist(@Body request: WishlistRequest): retrofit2.Response<WishlistToggleResponse>

    @GET("wishlist.php")
    suspend fun getMyWishlist(@retrofit2.http.Query("user_id") userId: Int): retrofit2.Response<ProductListResponse>

    @GET("get_settings.php")
    suspend fun getAppSettings(): retrofit2.Response<SettingsResponse>

    @POST("api_manage_products.php")
    suspend fun manageProduct(@Body request: ManageProductRequest): retrofit2.Response<ApiResponse>

    @POST("api_admin_master.php")
    suspend fun sendAdminCommand(@Body request: AdminMasterRequest): retrofit2.Response<okhttp3.ResponseBody>
}

object ApiErrorEvent {
    private val _events = kotlinx.coroutines.flow.MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: kotlinx.coroutines.flow.SharedFlow<String> = _events
    
    fun emit(message: String) {
        _events.tryEmit(message)
    }
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

    private val ErrorInterceptor = okhttp3.Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)
        if (!response.isSuccessful) {
            val responseBody = response.peekBody(Long.MAX_VALUE).string()
            android.util.Log.e("API_ERROR", "Code: ${response.code}, URL: ${request.url}, Body: $responseBody")
            
            val userFriendlyMessage = when (response.code) {
                in 500..599 -> "Server issues. Please try again later. (Error ${response.code})"
                in 400..499 -> "Invalid request or credentials. (Error ${response.code})"
                else -> "API Error: ${response.code} ${response.message}"
            }
            val urlString = request.url.toString()
            if (!urlString.contains("log_visitor.php") && !urlString.contains("get_settings.php") && !urlString.contains("api_chat.php") && !urlString.contains("api_orders.php") && !urlString.contains("login.php") && !urlString.contains("register.php")) {
                ApiErrorEvent.emit(userFriendlyMessage)
            }
        }
        response
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(securityInterceptor)
        .addInterceptor(ErrorInterceptor)
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
