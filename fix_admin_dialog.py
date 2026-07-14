import re

with open('app/src/main/java/com/example/ui/screens/AdminAppScreens.kt', 'r') as f:
    content = f.read()

target = """                                                    Text("Address: ${order.deliveryAddress ?: "N/A"}")
                                                    Text("Date: ${order.createdAt}")
                                                    Text("Status: ${order.status}")"""

replacement = """                                                    Text("Address: ${order.deliveryAddress ?: "N/A"}")
                                                    Text("Pickup Schedule: ${order.pickupSchedule ?: "N/A"}")
                                                    Text("Delivery Schedule: ${order.deliverySchedule ?: "N/A"}")
                                                    Text("Date: ${order.createdAt}")
                                                    Text("Status: ${order.status}")"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/AdminAppScreens.kt', 'w') as f:
    f.write(content)
