package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.viewmodel.MarketViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(viewModel: MarketViewModel) {
    var orders by remember { mutableStateOf<List<com.example.network.NetworkOrder>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Manage Orders & Dashboard", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Dashboard Chart
            if (orders.isNotEmpty()) {
                val totalRevenue = orders.filter { !it.status.equals("Cancelled", ignoreCase = true) }.sumOf { it.totalAmount }
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("30-Day Overview", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Total Revenue", color = MaterialTheme.colorScheme.outline)
                                Text("Rs. ${String.format(java.util.Locale.US, "%.2f", totalRevenue)}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Orders", color = MaterialTheme.colorScheme.outline)
                                Text("${orders.size}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        
                        // Fake Bar Chart with Canvas
                        Spacer(modifier = Modifier.height(16.dp))
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                            val barWidth = 24.dp.toPx()
                            val spacing = 16.dp.toPx()
                            val maxBarHeight = size.height
                            
                            val dummyData = listOf(0.4f, 0.7f, 0.3f, 0.8f, 0.5f, 0.9f, 0.6f)
                            val totalWidth = (dummyData.size * barWidth) + ((dummyData.size - 1) * spacing)
                            val startX = (size.width - totalWidth) / 2
                            
                            dummyData.forEachIndexed { index, fillPercent ->
                                val x = startX + (index * (barWidth + spacing))
                                val barHeight = maxBarHeight * fillPercent
                                val y = size.height - barHeight
                                
                                drawRect(
                                    color = androidx.compose.ui.graphics.Color(0xFF4CAF50).copy(alpha = 0.8f),
                                    topLeft = androidx.compose.ui.geometry.Offset(x, y),
                                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                                )
                            }
                        }
                    }
                }
            }

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
                                Text("Total: Rs. ${order.totalAmount}")
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
                                                            orderId = order.id.toIntOrNull(),
                                                            status = status
                                                        )
                                                        val res = viewModel.sendAdminCommand(request)
                                                        if (res?.status == "success") {
                                                            snackbarHostState.showSnackbar("Order status updated successfully!")
                                                        } else {
                                                            snackbarHostState.showSnackbar("Failed to update order status.")
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
}

@Composable
fun AdminSettingsScreen(viewModel: MarketViewModel) {
    var maintenanceMode by remember { mutableStateOf(false) }
    var codEnabled by remember { mutableStateOf(true) }
    var appName by remember { mutableStateOf("SnowWhite Boutique") }
    var deliveryFee by remember { mutableStateOf("10.0") }
    var primaryColor by remember { mutableStateOf("#4CAF50") }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                            snackbarHostState.showSnackbar("Settings Updated!")
                        } else {
                            snackbarHostState.showSnackbar("Failed to update settings.")
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
                        label = { Text("Base Delivery Fee (Rs)") },
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

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoriesScreen(viewModel: MarketViewModel, onBack: () -> Unit) {
    val categories by viewModel.appCategories.collectAsState()
    var newCategoryName by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("Star") }
    val builtinIcons = listOf("Star", "LocalLaundryService", "Iron", "DryCleaning", "Checkroom", "HomeRepairService")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Categories") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("New Category") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newCategoryName.isNotBlank()) {
                                viewModel.addCategory(newCategoryName.trim(), selectedIcon)
                                newCategoryName = ""
                            }
                        }
                    ) {
                        Text("Add")
                    }
                }
                
                // Icon Picker
                Spacer(modifier = Modifier.height(12.dp))
                Text("Select Icon:", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    builtinIcons.forEach { iconName ->
                        val isSelected = selectedIcon == iconName
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedIcon = iconName },
                            label = { Text(iconName, fontSize = 12.sp) }
                        )
                    }
                }
            }

            androidx.compose.foundation.lazy.LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories.size) { idx ->
                    val cat = categories[idx]
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Dummy visual for icon (in real app map string to ImageVector)
                                Box(
                                    modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primaryContainer, androidx.compose.foundation.shape.CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(cat.iconName.take(1), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(cat.name, style = MaterialTheme.typography.bodyLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            }
                            IconButton(onClick = { viewModel.deleteCategory(cat.name) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
