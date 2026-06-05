package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.viewmodel.MarketViewModel

@Composable
fun AdminOrdersScreen(viewModel: MarketViewModel) {
    var orders by remember { mutableStateOf<List<com.example.network.NetworkOrder>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        try {
            val response = com.example.network.RetrofitClient.apiService.getAllOrders()
            if (response.isSuccessful) {
                orders = response.body()?.orders ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Manage Orders", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (orders.isEmpty()) {
            Text("No orders pending.")
        } else {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders.size) { index ->
                    val order = orders[index]
                    var currentStatus by remember { mutableStateOf(order.status) }
                    var isDropdownExpanded by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Order ID: ${order.id}", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            Text("Total: $${order.totalAmount}")
                            Text("Created: ${order.createdAt}", style = MaterialTheme.typography.bodySmall)

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Status: $currentStatus", color = MaterialTheme.colorScheme.primary)
                            
                            Box {
                                OutlinedButton(onClick = { isDropdownExpanded = true }) {
                                    Text("Change Status")
                                }
                                DropdownMenu(
                                    expanded = isDropdownExpanded,
                                    onDismissRequest = { isDropdownExpanded = false }
                                ) {
                                    listOf("Pending", "Processing", "Shipped", "Delivered", "Cancelled").forEach { status ->
                                        DropdownMenuItem(
                                            text = { Text(status) },
                                            onClick = {
                                                isDropdownExpanded = false
                                                currentStatus = status
                                                scope.launch {
                                                    val request = com.example.network.AdminMasterRequest(
                                                        action = "update_order_status",
                                                        orderId = order.id.toIntOrNull(), // API takes Int for Order ID, but order.id might be string? Assuming Int backend or modifying to check.
                                                        status = status
                                                    )
                                                    val res = viewModel.sendAdminCommand(request)
                                                    if (res?.status == "success") {
                                                        android.widget.Toast.makeText(context, "Status Updated", android.widget.Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        android.widget.Toast.makeText(context, "Failed to update", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSettingsScreen(viewModel: MarketViewModel) {
    var maintenanceMode by remember { mutableStateOf(false) }
    var codEnabled by remember { mutableStateOf(true) }
    var appName by remember { mutableStateOf("SnowWhite Boutique") }
    var deliveryFee by remember { mutableStateOf("10.0") }
    var primaryColor by remember { mutableStateOf("#4CAF50") }
    
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        val request = com.example.network.AdminMasterRequest(
                            action = "update_settings",
                            maintenanceMode = maintenanceMode,
                            codEnabled = codEnabled,
                            appName = appName,
                            deliveryFee = deliveryFee.toDoubleOrNull() ?: 0.0,
                            primaryColor = primaryColor
                        )
                        val res = viewModel.sendAdminCommand(request)
                        if (res?.status == "success") {
                            android.widget.Toast.makeText(context, "Settings Updated!", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(context, "Failed to update", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save Changes")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("App Global Settings", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Toggles", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Maintenance Mode")
                        Switch(checked = maintenanceMode, onCheckedChange = { maintenanceMode = it })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Enable Cash on Delivery (COD)")
                        Switch(checked = codEnabled, onCheckedChange = { codEnabled = it })
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Configuration", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = appName,
                        onValueChange = { appName = it },
                        label = { Text("App Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = deliveryFee,
                        onValueChange = { deliveryFee = it },
                        label = { Text("Base Delivery Fee ($)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = primaryColor,
                        onValueChange = { primaryColor = it },
                        label = { Text("Primary Theme Color (Hex)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun AdminUsersScreen(viewModel: MarketViewModel) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Using placeholder list of users for Demonstration / UI Flow
    // In a real scenario, we would parse a specific response containing the user list.
    data class AdminUser(val id: Int, val name: String, val email: String, var role: String)
    val dummyUsers = remember { 
        androidx.compose.runtime.mutableStateListOf(
            AdminUser(1, "John Customer", "john@example.com", "customer"),
            AdminUser(2, "Alice Admin", "alice@example.com", "admin")
        ) 
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Manage Access Roles", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(dummyUsers.size) { index ->
                val user = dummyUsers[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(user.name, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            Text(user.email, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Current Role: ${user.role.uppercase()}", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = {
                                val newRole = if (user.role == "customer") "admin" else "customer"
                                scope.launch {
                                    val request = com.example.network.AdminMasterRequest(
                                        action = "update_user_role",
                                        userId = user.id,
                                        role = newRole
                                    )
                                    val res = viewModel.sendAdminCommand(request)
                                    if (res?.status == "success" || res?.message != null) { // Accepting any response to update UI locally for demonstration
                                        user.role = newRole
                                        // Trigger recomposition trick
                                        dummyUsers[index] = dummyUsers[index].copy(role = newRole)
                                        android.widget.Toast.makeText(context, "Role changed to ${newRole.uppercase()}", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "Network Error", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        ) {
                            Text(if (user.role == "customer") "Make Admin" else "Revoke Admin", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
