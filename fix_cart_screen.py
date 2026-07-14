import re

with open('app/src/main/java/com/example/ui/screens/MarketScreens.kt', 'r') as f:
    content = f.read()

target1 = """    var addressStr by remember { mutableStateOf("") }"""

replacement1 = """    var addressStr by remember { mutableStateOf("") }
    var pickupDate by remember { mutableStateOf("") }
    var pickupTime by remember { mutableStateOf("") }
    var deliveryDate by remember { mutableStateOf("") }
    var deliveryTime by remember { mutableStateOf("") }"""

target2 = """                                            viewModel.checkout(
                                                paymentMethod = selectedPaymentMethod,
                                                shippingAddress = addressStr,
                                                phone = phoneStr,
                                                fullName = fullNameStr,
                                                onSuccess = { onNavigateToTab(3) }
                                            )"""

replacement2 = """                                            viewModel.checkout(
                                                paymentMethod = selectedPaymentMethod,
                                                shippingAddress = addressStr,
                                                phone = phoneStr,
                                                fullName = fullNameStr,
                                                pickupSchedule = "$pickupDate $pickupTime",
                                                deliverySchedule = "$deliveryDate $deliveryTime",
                                                onSuccess = { onNavigateToTab(3) }
                                            )"""

target3 = """                                OutlinedTextField(
                                    value = addressStr,
                                    onValueChange = { addressStr = it },
                                    label = { Text("Delivery Address") },
                                    placeholder = { Text("123 Main St, Apt 4B") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .testTag("address_input"),
                                    maxLines = 3,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.height(20.dp))"""

replacement3 = """                                OutlinedTextField(
                                    value = addressStr,
                                    onValueChange = { addressStr = it },
                                    label = { Text("Pickup & Delivery Address") },
                                    placeholder = { Text("123 Main St, Apt 4B") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .testTag("address_input"),
                                    maxLines = 3,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Scheduling", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = pickupDate,
                                        onValueChange = { pickupDate = it },
                                        label = { Text("Pickup Date") },
                                        placeholder = { Text("YYYY-MM-DD") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    OutlinedTextField(
                                        value = pickupTime,
                                        onValueChange = { pickupTime = it },
                                        label = { Text("Pickup Time") },
                                        placeholder = { Text("HH:MM") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = deliveryDate,
                                        onValueChange = { deliveryDate = it },
                                        label = { Text("Delivery Date") },
                                        placeholder = { Text("YYYY-MM-DD") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    OutlinedTextField(
                                        value = deliveryTime,
                                        onValueChange = { deliveryTime = it },
                                        label = { Text("Delivery Time") },
                                        placeholder = { Text("HH:MM") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(20.dp))"""

target4 = """                                                    viewModel.checkout(
                                                        paymentMethod = selectedPaymentMethod,
                                                        shippingAddress = addressStr,
                                                        phone = phoneStr,
                                                        fullName = fullNameStr,
                                                        onSuccess = { onNavigateToTab(3) }
                                                    )"""

replacement4 = """                                                    viewModel.checkout(
                                                        paymentMethod = selectedPaymentMethod,
                                                        shippingAddress = addressStr,
                                                        phone = phoneStr,
                                                        fullName = fullNameStr,
                                                        pickupSchedule = "$pickupDate $pickupTime",
                                                        deliverySchedule = "$deliveryDate $deliveryTime",
                                                        onSuccess = { onNavigateToTab(3) }
                                                    )"""


content = content.replace(target1, replacement1)
content = content.replace(target2, replacement2)
content = content.replace(target3, replacement3)
content = content.replace(target4, replacement4)

with open('app/src/main/java/com/example/ui/screens/MarketScreens.kt', 'w') as f:
    f.write(content)
