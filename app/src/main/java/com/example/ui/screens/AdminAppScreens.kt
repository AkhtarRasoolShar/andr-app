package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.withContext
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.viewmodel.MarketViewModel

data class AdminUser(val id: Int, val name: String, val email: String, var role: String)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(viewModel: MarketViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val orders by viewModel.adminAllOrdersState.collectAsState()

    if (orders == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val safeOrders = orders ?: emptyList()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Manage Orders & Dashboard", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Dashboard Chart
            if (safeOrders.isNotEmpty()) {
                val totalRevenue = safeOrders.filter { !it.status.equals("Cancelled", ignoreCase = true) }.sumOf { it.totalAmount }
                val completedOrders = safeOrders.count { it.status.equals("Delivered", ignoreCase = true) }
                val pendingOrders = safeOrders.count { it.status.equals("Pending", ignoreCase = true) }
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Performance Overview", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("Total Revenue", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                Text("Rs. ${String.format(java.util.Locale.US, "%.2f", totalRevenue)}", style = MaterialTheme.typography.headlineSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Orders", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                Text("${safeOrders.size}", style = MaterialTheme.typography.headlineSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("Delivered", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                Text("$completedOrders", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Pending", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                Text("$pendingOrders", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                }
            }

            if (safeOrders.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No orders pending.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search by Order ID or Customer Name") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                )

                val filteredOrders = safeOrders.filter { order ->
                    order.id.contains(searchQuery, ignoreCase = true) ||
                    (order.customerName?.contains(searchQuery, ignoreCase = true) == true)
                }

                if (filteredOrders.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No matching orders found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredOrders.size) { index ->
                            val order = filteredOrders[index]
                            var currentStatus by remember { mutableStateOf(order.status) }
                            var isDropdownExpanded by remember { mutableStateOf(false) }
                            var showViewOrderDialog by remember { mutableStateOf(false) }

                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { showViewOrderDialog = true },
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("Order ID: ${order.id}", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                        androidx.compose.material3.Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                            Text(currentStatus, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Customer: ${order.customerName ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Total: Rs. ${order.totalAmount}", style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Created: ${order.createdAt}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        OutlinedButton(onClick = { showViewOrderDialog = true }, modifier = Modifier.padding(end = 8.dp)) {
                                            Text("View")
                                        }
                                        Box {
                                            FilledTonalButton(onClick = { isDropdownExpanded = true }) {
                                                Text("Update")
                                            }

                                            DropdownMenu(
                                                expanded = isDropdownExpanded,
                                                onDismissRequest = { isDropdownExpanded = false }
                                            ) {
                                                listOf("Pending", "Processing", "Shipped", "Delivered", "Cancelled").forEach { status ->
                                                    DropdownMenuItem(
                                                        text = { Text("Set: $status") },
                                                        onClick = {
                                                            isDropdownExpanded = false
                                                            currentStatus = status
                                                            scope.launch {
                                                                val request = com.example.network.AdminMasterRequest(
                                                                    action = "update_order_status",
                                                                    orderId = order.id.toIntOrNull(),
                                                                    status = status
                                                                )
                                                                val resString = viewModel.sendAdminCommandString(request)
                                                                val isSuccess = resString != null && org.json.JSONObject(resString).optString("status") == "success"
                                                                if (isSuccess) {
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
                                    
                                    if (showViewOrderDialog) {
                                        AlertDialog(
                                            onDismissRequest = { showViewOrderDialog = false },
                                            title = { Text("Order Details - #${order.id}") },
                                            text = {
                                                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Text("Customer: ${order.customerName ?: "N/A"}", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                                                    Text("Phone: ${order.phone ?: "N/A"}")
                                                    Text("Address: ${order.deliveryAddress ?: "N/A"}")
                                                    Text("Date: ${order.createdAt}")
                                                    Text("Status: ${order.status}")
                                                    Text("Payment Method: ${order.paymentMethod ?: "COD"}")
                                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                                    Text("Items:", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                                    if (order.items.isNullOrEmpty()) {
                                                        Text("No items info available.")
                                                    } else {
                                                        order.items.forEach { item ->
                                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                                Text("${item.quantity}x ${item.name ?: "Item"}")
                                                                Text("Rs. ${item.price ?: 0.0}")
                                                            }
                                                        }
                                                    }
                                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                                    Text("Total Amount: Rs. ${order.totalAmount}", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                }
                                            },
                                            confirmButton = {
                                                TextButton(onClick = {
                                                    printOrderReceipt(context, order)
                                                    showViewOrderDialog = false
                                                }) { Text("Print PDF") }
                                            },
                                            dismissButton = {
                                                TextButton(onClick = { showViewOrderDialog = false }) { Text("Close") }
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
    var logoUrl by remember { mutableStateOf("") }
    var deliveryFee by remember { mutableStateOf("10.0") }
    var primaryColor by remember { mutableStateOf("#4CAF50") }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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
                        value = logoUrl,
                        onValueChange = { logoUrl = it },
                        label = { Text("App Logo URL") },
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

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    scope.launch {
                        val request = com.example.network.AdminMasterRequest(
                            action = "update_settings",
                            maintenanceMode = maintenanceMode,
                            codEnabled = codEnabled,
                            appName = appName,
                            logoUrl = logoUrl,
                            deliveryFee = deliveryFee.toDoubleOrNull() ?: 0.0,
                            primaryColor = primaryColor
                        )
                        val resString = viewModel.sendAdminCommandString(request)
                        val isSuccess = resString != null && org.json.JSONObject(resString).optString("status") == "success"
                        if (isSuccess) {
                            snackbarHostState.showSnackbar("Settings Updated!")
                        } else {
                            snackbarHostState.showSnackbar("Failed to update settings.")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save Changes")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Settings")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout Admin")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(viewModel: MarketViewModel, onChatClick: (Int) -> Unit) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    val activeChats = remember { androidx.compose.runtime.mutableStateListOf<com.example.network.ActiveChatUser>() }
    var isLoading by remember { mutableStateOf(true) }

    var floatingChatUser by remember { mutableStateOf<com.example.network.ActiveChatUser?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        while (true) {
            val chats = viewModel.getActiveChats()
            activeChats.clear()
            // Sort by last message time descending. If no time, put them at the end.
            val sortedChats = chats.sortedByDescending { it.lastMessageTime ?: "" }
            activeChats.addAll(sortedChats)
            isLoading = false
            kotlinx.coroutines.delay(3000)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Active User Chats", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (activeChats.isEmpty()) {
                Text("No active chats found.")
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(activeChats.size) { index ->
                        val user = activeChats[index]
                        Card(
                            modifier = Modifier.fillMaxWidth()
                                .clickable { floatingChatUser = user },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = user.name ?: "", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                        if (user.unreadCount != null && user.unreadCount > 0) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            androidx.compose.material3.Badge(containerColor = MaterialTheme.colorScheme.error) {
                                                Text(text = user.unreadCount.toString(), color = MaterialTheme.colorScheme.onError)
                                            }
                                        }
                                    }
                                    Text(text = user.email ?: "", style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Last Message: ${user.lastMessage ?: ""}", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = user.lastMessageTime ?: "", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelSmall)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Button(onClick = { floatingChatUser = user }) {
                                        Text("Quick Reply", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (floatingChatUser != null) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { floatingChatUser = null }) {
                Card(
                    modifier = Modifier.fillMaxWidth().height(500.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(floatingChatUser!!.name, color = MaterialTheme.colorScheme.onPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, modifier = Modifier.weight(1f))
                            IconButton(onClick = { floatingChatUser = null }) {
                                Icon(Icons.Filled.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                        
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            AdminQuickChatPanel(viewModel = viewModel, adminUserId = 1, customerUserId = floatingChatUser!!.userId)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminQuickChatPanel(viewModel: MarketViewModel, adminUserId: Int, customerUserId: Int) {
    var inputText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val coroutineScope = rememberCoroutineScope()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(customerUserId) {
        while (true) {
            try {
                val apiMsgs = viewModel.getChatHistory(adminUserId, customerUserId)
                val newMsgs = apiMsgs.map { networkMsg ->
                    val isMine = networkMsg.senderId == adminUserId
                    ChatMessage(networkMsg.message, isMine, networkId = networkMsg.id)
                }
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    val existingIds = messages.mapNotNull { it.networkId }.toSet()
                    val toProcess = newMsgs.filterNot { it.networkId != null && it.networkId in existingIds }
                    
                    val toAdd = mutableListOf<ChatMessage>()
                    for (msg in toProcess) {
                        val localMatchIdx = messages.indexOfLast { it.isUser == msg.isUser && it.networkId == null && it.text == msg.text }
                        if (localMatchIdx != -1) {
                            messages[localMatchIdx] = messages[localMatchIdx].copy(networkId = msg.networkId)
                        } else {
                            toAdd.add(msg)
                        }
                    }
                    if (toAdd.isNotEmpty()) {
                        messages.addAll(toAdd)
                    }
                }
            } catch (e: Exception) {}
            kotlinx.coroutines.delay(2000)
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        androidx.compose.foundation.lazy.LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages.size) { index ->
                val msg = messages[index]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = msg.text,
                            color = if (msg.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type quick reply...") },
                singleLine = true,
                maxLines = 1,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Send
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSend = {
                        if (inputText.isNotBlank()) {
                            val text = inputText
                            inputText = ""
                            messages.add(ChatMessage(text, true))
                            coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                viewModel.sendChatMessage(adminUserId, customerUserId, text)
                            }
                        }
                    }
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            FloatingActionButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        val text = inputText
                        inputText = ""
                        messages.add(ChatMessage(text, true))
                        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            viewModel.sendChatMessage(adminUserId, customerUserId, text)
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(50.dp)
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Send")
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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

fun printOrderReceipt(context: android.content.Context, order: com.example.network.NetworkOrder) {
    val webView = android.webkit.WebView(context)

    val htmlDocument = "" +
        "<html>" +
        "<head>" +
        "    <style>" +
        "        body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; padding: 20px; color: #333; }" +
        "        .header { text-align: center; border-bottom: 2px solid #333; padding-bottom: 10px; margin-bottom: 20px; }" +
        "        .header h1 { margin: 0; font-size: 24px; }" +
        "        .order-info { margin-bottom: 20px; font-size: 14px; }" +
        "        .order-info div { margin-bottom: 5px; }" +
        "        .total { text-align: right; font-size: 18px; font-weight: bold; margin-top: 20px; }" +
        "        .footer { text-align: center; font-size: 12px; color: #777; margin-top: 40px; border-top: 1px solid #eee; padding-top: 10px; }" +
        "    </style>" +
        "</head>" +
        "<body>" +
        "    <div class=\"header\">" +
        "        <h1>SnowWhite Boutique</h1>" +
        "        <div>Order Receipt</div>" +
        "    </div>" +
        "    " +
        "    <div class=\"order-info\">" +
        "        <div><strong>Order ID:</strong> #${order.id}</div>" +
        "        <div><strong>Date:</strong> ${order.createdAt}</div>" +
        "        <div><strong>Customer Name:</strong> ${order.customerName ?: "N/A"}</div>" +
        "        <div><strong>Delivery Address:</strong> ${order.deliveryAddress ?: "N/A"}</div>" +
        "        <div><strong>Phone:</strong> ${order.phone ?: "N/A"}</div>" +
        "        <div><strong>Payment Method:</strong> ${order.paymentMethod ?: "N/A"}</div>" +
        "        <div><strong>Status:</strong> ${order.status}</div>" +
        "    </div>" +
        "    " +
        "    <div class=\"total\">" +
        "        Total Amount: Rs. ${order.totalAmount}" +
        "    </div>" +
        "    " +
        "    <div class=\"footer\">" +
        "        Thank you for your business!" +
        "    </div>" +
        "</body>" +
        "</html>"

    webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null)

    webView.webViewClient = object : android.webkit.WebViewClient() {
        override fun onPageFinished(view: android.webkit.WebView, url: String) {
            val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as android.print.PrintManager
            val printAdapter = webView.createPrintDocumentAdapter("Order_${order.id}_Receipt")
            val jobName = "Order_${order.id}_Slip"
            printManager.print(jobName, printAdapter, android.print.PrintAttributes.Builder().build())
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AdminSupportChatScreen(viewModel: MarketViewModel, onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    val messages = remember { androidx.compose.runtime.mutableStateListOf<com.example.ui.screens.ChatMessage>() }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    val customerUserId = viewModel.activeChatUserId ?: return // Cannot chat if unknown

    val adminUserId = 1

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(customerUserId) {
        while (true) {
            try {
                val apiMsgs = viewModel.getChatHistory(adminUserId, customerUserId) // admin id = dynamic, other user
                val newMsgs = apiMsgs.map { networkMsg ->
                    com.example.ui.screens.ChatMessage(
                        text = networkMsg.message,
                        isUser = networkMsg.senderId == adminUserId, // User is "self" for UI drawing
                        networkId = networkMsg.id
                    )
                }
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    val existingIds = messages.mapNotNull { it.networkId }.toSet()
                    val toProcess = newMsgs.filterNot { it.networkId != null && it.networkId in existingIds }
                    
                    val toAdd = mutableListOf<com.example.ui.screens.ChatMessage>()
                    for (msg in toProcess) {
                        val localMatchIdx = messages.indexOfLast { it.isUser == msg.isUser && it.networkId == null && it.text == msg.text }
                        if (localMatchIdx != -1) {
                            messages[localMatchIdx] = messages[localMatchIdx].copy(networkId = msg.networkId)
                        } else {
                            toAdd.add(msg)
                        }
                    }
                    if (toAdd.isNotEmpty()) {
                        messages.addAll(toAdd)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            kotlinx.coroutines.delay(3000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat with User #$customerUserId") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            androidx.compose.foundation.lazy.LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages.size) { i ->
                    com.example.ui.screens.ChatBubble(messages[i])
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type here...") },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (inputText.isEmpty() && messages.isNotEmpty()) {
                                // Auto-fill with AI suggestion
                                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                    val lastUserMsg = messages.lastOrNull { !it.isUser }?.text ?: ""
                                    if (lastUserMsg.isNotEmpty()) {
                                        withContext(kotlinx.coroutines.Dispatchers.Main) { inputText = "Generating reply... " }
                                        try {
                                            val prompt = "You are a customer support admin for SnowWhite Boutique answering a user. The user said: \"$lastUserMsg\". Reply briefly and warmly as the human admin."
                                            val request = com.example.network.GenerateContentRequest(
                                                contents = listOf(com.example.network.Content(parts = listOf(com.example.network.Part(text = prompt))))
                                            )
                                            val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                                            val response = com.example.network.GeminiRetrofitClient.service.generateContent(apiKey, request)
                                            val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "I am looking into this for you."
                                            withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                inputText = reply
                                            }
                                        } catch (e: Exception) {
                                            withContext(kotlinx.coroutines.Dispatchers.Main) { inputText = "" }
                                        }
                                    }
                                }
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = "AI Suggestion")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = {
                            val userText = inputText.replace("Generating reply... ", "").trim()
                            if (userText.isNotEmpty()) {
                                messages.add(com.example.ui.screens.ChatMessage(userText, true))
                                inputText = ""
                                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                    val adminId = 1
                                    viewModel.sendChatMessage(adminId, customerUserId, userText)
                                }
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}
