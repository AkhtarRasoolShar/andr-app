package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.network.NetworkOrder
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.network.RetrofitClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(onBack: () -> Unit) {
    var searchId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var orderData by remember { mutableStateOf<NetworkOrder?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Poll if orderData is present
    LaunchedEffect(orderData?.id) {
        if (orderData?.id != null) {
            while(true) {
                kotlinx.coroutines.delay(10000)
                try {
                    val orderIdInt = orderData!!.id.replace(Regex("[^0-9]"), "").toIntOrNull()
                    if (orderIdInt != null) {
                        val res = RetrofitClient.apiService.getOrderDetail(orderIdInt)
                        if (res.isSuccessful && res.body()?.success == true && !res.body()?.orders.isNullOrEmpty()) {
                            orderData = res.body()!!.orders!!.first()
                        }
                    }
                } catch(e: Exception) {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Order") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = searchId,
                onValueChange = { searchId = it },
                label = { Text("Enter Tracking ID (e.g. SNOW-ORD-123)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = {
                        if (searchId.isNotBlank()) {
                            isLoading = true
                            errorMessage = null
                            orderData = null
                            scope.launch(Dispatchers.IO) {
                                try {
                                    val idInt = searchId.replace(Regex("[^0-9]"), "").toIntOrNull()
                                    if (idInt != null) {
                                        val response = RetrofitClient.apiService.getOrderDetail(idInt)
                                        withContext(Dispatchers.Main) {
                                            if (response.isSuccessful && response.body()?.success == true && !response.body()?.orders.isNullOrEmpty()) {
                                                orderData = response.body()!!.orders!!.first()
                                            } else {
                                                errorMessage = "Order not found."
                                            }
                                            isLoading = false
                                        }
                                    } else {
                                        withContext(Dispatchers.Main) {
                                            errorMessage = "Invalid Order ID."
                                            isLoading = false
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        errorMessage = "Failed to fetch order. Check your connection."
                                        isLoading = false
                                    }
                                }
                            }
                        }
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else if (errorMessage != null) {
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
            } else if (orderData != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Order ID: ${orderData!!.id}", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Status: ${orderData!!.status.uppercase()}", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Total Amount: $${orderData!!.totalAmount}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Date: ${orderData!!.createdAt}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
