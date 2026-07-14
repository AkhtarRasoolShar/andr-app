import re

with open('app/src/main/java/com/example/ui/screens/MarketScreens.kt', 'r') as f:
    content = f.read()

target1 = """                    val (bgColor, textColor) = when (order.status.lowercase()) {
                        "pending" -> Color(0xFFFFF59D) to Color(0xFFF57F17)
                        "processing" -> Color(0xFFBBDEFB) to Color(0xFF1565C0)
                        "shipped" -> Color(0xFFE1BEE7) to Color(0xFF6A1B9A)
                        "delivered" -> Color(0xFFC8E6C9) to Color(0xFF2E7D32)
                        "cancelled" -> Color(0xFFFFCDD2) to Color(0xFFC62828)
                        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                    }"""

replacement1 = """                    val (bgColor, textColor) = when (order.status.lowercase()) {
                        "pending" -> Color(0xFFFFF59D) to Color(0xFFF57F17)
                        "picked up" -> Color(0xFFFFF59D) to Color(0xFFF57F17)
                        "processing" -> Color(0xFFBBDEFB) to Color(0xFF1565C0)
                        "in process" -> Color(0xFFBBDEFB) to Color(0xFF1565C0)
                        "shipped" -> Color(0xFFE1BEE7) to Color(0xFF6A1B9A)
                        "out for delivery" -> Color(0xFFE1BEE7) to Color(0xFF6A1B9A)
                        "delivered" -> Color(0xFFC8E6C9) to Color(0xFF2E7D32)
                        "cancelled" -> Color(0xFFFFCDD2) to Color(0xFFC62828)
                        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                    }"""

target2 = """            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Items Ordered: ","""

replacement2 = """            Spacer(modifier = Modifier.height(16.dp))
            if (order.pickupSchedule.isNotBlank() || order.deliverySchedule.isNotBlank()) {
                Text("Pickup: ${order.pickupSchedule}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                Text("Delivery: ${order.deliverySchedule}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(
                text = "Items Ordered: ","""

content = content.replace(target1, replacement1)
content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/ui/screens/MarketScreens.kt', 'w') as f:
    f.write(content)
