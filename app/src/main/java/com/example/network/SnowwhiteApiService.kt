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
    val passwordEntered: String
)

data class UserProfileResponse(
    val email: String,
    val fullName: String,
    val phoneNumber: String,
    val city: String,
    val deliveryAddress: String,
    val membershipPoints: Int = 100,
    val role: String = "customer"
)

data class LoginResponse(
    val success: Boolean,
    val message: String? = null,
    val role: String? = null,
    val user: UserProfileResponse? = null
)

data class RegisterRequest(
    val email: String,
    val fullName: String,
    val passwordEntered: String,
    val phoneNumber: String,
    val city: String,
    val deliveryAddress: String
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

data class UploadImageRequest(
    val image: String // base64-encoded string representation
)

data class UploadImageResponse(
    val success: Boolean,
    @Json(name = "image_url") val imageUrl: String? = null,
    val message: String? = null
)

data class NetworkCartItem(
    @Json(name = "product_id") val productId: Int,
    val quantity: Int
)

data class OrderRequest(
    val email: String,
    @Json(name = "shipping_address") val shippingAddress: String,
    @Json(name = "payment_method") val payment_method: String = "cod",
    @Json(name = "payment_card_last4") val paymentCardLast4: String = "",
    @Json(name = "pickup_schedule") val pickupSchedule: String = "",
    @Json(name = "delivery_schedule") val deliverySchedule: String = "",
    val items: List<NetworkCartItem>
)

data class OrderResponse(
    val success: Boolean,
    val message: String? = null,
    @Json(name = "order_id") val orderId: String? = null
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

interface SnowwhiteApi {
    @POST("login.php")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("register.php")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @GET("get_products.php")
    suspend fun getProducts(): List<ProductResponse>

    @POST("add_product.php")
    suspend fun addProduct(@Body request: AddProductRequest): AddProductResponse

    @POST("upload_image.php")
    suspend fun uploadImage(@Body request: UploadImageRequest): UploadImageResponse

    @POST("place_order.php")
    suspend fun placeOrder(@Body request: OrderRequest): OrderResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://akhtarhussain.site/api/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
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
