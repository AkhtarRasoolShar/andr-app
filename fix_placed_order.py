import re

with open('app/src/main/java/com/example/viewmodel/MarketViewModel.kt', 'r') as f:
    content = f.read()

target = """                val newOrder = com.example.data.Order(
                    id = orderId,
                    timestamp = System.currentTimeMillis(),
                    itemsSummary = itemDetails.ifBlank { "Order from Checkout" },
                    totalAmount = currentSummary.total,
                    status = "Placed",
                    paymentCardLast4 = paymentMethod,
                    shippingAddress = shippingAddress
                )"""

replacement = """                val newOrder = com.example.data.Order(
                    id = orderId,
                    timestamp = System.currentTimeMillis(),
                    itemsSummary = itemDetails.ifBlank { "Order from Checkout" },
                    totalAmount = currentSummary.total,
                    status = "Placed",
                    paymentCardLast4 = paymentMethod,
                    shippingAddress = shippingAddress,
                    pickupSchedule = pickupSchedule,
                    deliverySchedule = deliverySchedule
                )"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/viewmodel/MarketViewModel.kt', 'w') as f:
    f.write(content)
