import re

with open('app/src/main/java/com/example/network/SnowwhiteApiService.kt', 'r') as f:
    content = f.read()

target1 = """data class OrderRequest(
    @field:Json(name = "user_id") val userId: Int,
    @field:Json(name = "total_amount") val totalAmount: Double,
    @field:Json(name = "payment_method") val paymentMethod: String = "COD",
    val address: String,
    val phone: String,
    @field:Json(name = "guest_name") val guestName: String? = null,
    @field:Json(name = "items") val items: List<NetworkCartItem>
)"""

replacement1 = """data class OrderRequest(
    @field:Json(name = "user_id") val userId: Int,
    @field:Json(name = "total_amount") val totalAmount: Double,
    @field:Json(name = "payment_method") val paymentMethod: String = "COD",
    val address: String,
    val phone: String,
    @field:Json(name = "guest_name") val guestName: String? = null,
    @field:Json(name = "pickup_schedule") val pickupSchedule: String? = null,
    @field:Json(name = "delivery_schedule") val deliverySchedule: String? = null,
    @field:Json(name = "items") val items: List<NetworkCartItem>
)"""

target2 = """data class NetworkOrder(
    val id: String,
    @field:Json(name = "total_amount") val totalAmount: Double,
    val status: String,
    @field:Json(name = "created_at") val createdAt: String,
    @field:Json(name = "customer_name") val customerName: String? = null,
    val phone: String? = null,
    @field:Json(name = "delivery_address") val deliveryAddress: String? = null,
    @field:Json(name = "payment_method") val paymentMethod: String? = null,
    val items: List<NetworkOrderItem>? = null
)"""

replacement2 = """data class NetworkOrder(
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
)"""

content = content.replace(target1, replacement1)
content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/network/SnowwhiteApiService.kt', 'w') as f:
    f.write(content)
