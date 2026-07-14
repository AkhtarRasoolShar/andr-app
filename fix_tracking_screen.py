import re

with open('app/src/main/java/com/example/ui/screens/TrackingScreen.kt', 'r') as f:
    content = f.read()

target1 = """                        val currentStageIdx = when (orderData!!.status.lowercase()) {
                            "pending" -> 0
                            "processing" -> 1
                            "shipped" -> 2
                            "delivered" -> 2
                            else -> 0
                        }
                        
                        StatusTracker(currentStageIdx)"""

replacement1 = """                        val currentStageIdx = when (orderData!!.status.lowercase()) {
                            "pending" -> -1
                            "picked up" -> 0
                            "processing" -> 1
                            "shipped" -> 2
                            "out for delivery" -> 2
                            "delivered" -> 3
                            else -> -1
                        }
                        
                        StatusTracker(currentStageIdx)"""

target2 = """fun StatusTracker(currentStageIdx: Int) {
    val stages = listOf("Picked Up", "In Process", "Out for Drop-off")"""

replacement2 = """fun StatusTracker(currentStageIdx: Int) {
    val stages = listOf("Picked Up", "In Process", "Out for Delivery")"""

content = content.replace(target1, replacement1)
content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/ui/screens/TrackingScreen.kt', 'w') as f:
    f.write(content)
