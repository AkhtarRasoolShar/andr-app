import re

with open('app/src/main/java/com/example/viewmodel/MarketViewModel.kt', 'r') as f:
    content = f.read()

target1 = """    fun checkout(
        paymentMethod: String,
        shippingAddress: String,
        phone: String,
        fullName: String,
        onSuccess: () -> Unit
    ) {"""

replacement1 = """    fun checkout(
        paymentMethod: String,
        shippingAddress: String,
        phone: String,
        fullName: String,
        pickupSchedule: String = "",
        deliverySchedule: String = "",
        onSuccess: () -> Unit
    ) {"""

target2 = """                val request = com.example.network.OrderRequest(
                    userId = userId,
                    totalAmount = currentSummary.total,
                    paymentMethod = paymentMethod,
                    address = shippingAddress,
                    phone = phone,
                    guestName = fullName,
                    items = networkItems
                )"""

replacement2 = """                val request = com.example.network.OrderRequest(
                    userId = userId,
                    totalAmount = currentSummary.total,
                    paymentMethod = paymentMethod,
                    address = shippingAddress,
                    phone = phone,
                    guestName = fullName,
                    pickupSchedule = pickupSchedule,
                    deliverySchedule = deliverySchedule,
                    items = networkItems
                )"""

content = content.replace(target1, replacement1)
content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/viewmodel/MarketViewModel.kt', 'w') as f:
    f.write(content)
