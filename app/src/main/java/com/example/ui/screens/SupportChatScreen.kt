package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MarketViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

data class ChatMessage(val text: String, val isUser: Boolean, val timestamp: Long = System.currentTimeMillis())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportChatScreen(viewModel: MarketViewModel, onBack: () -> Unit) {
    var inputText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val coroutineScope = rememberCoroutineScope()

    // Init welcome message purely client-side
    LaunchedEffect(Unit) {
        messages.add(ChatMessage("Hello! I am your virtual assistant. How can I help you today?", false))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Virtual Support") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(msg)
                }
            }

            // Input field
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
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            val userText = inputText.trim()
                            if (userText.isNotEmpty()) {
                                messages.add(ChatMessage(userText, true))
                                inputText = ""
                                // Bot logic
                                val lower = userText.lowercase()
                                val settings = viewModel.appSettingsMap
                                
                                val orderIdMatch = Regex("(?:order(?: id)?[:\\s]*|SNOW-ORD-)(\\d+)", RegexOption.IGNORE_CASE).find(lower)
                                if (orderIdMatch != null) {
                                    val orderIdStr = orderIdMatch.groupValues[1]
                                    try {
                                        val orderId = orderIdStr.toInt()
                                        messages.add(ChatMessage("Checking order tracking for $orderId...", false))
                                        coroutineScope.launch(Dispatchers.IO) {
                                            try {
                                                val response = com.example.network.RetrofitClient.apiService.getOrderDetail(orderId)
                                                val body = response.body()
                                                val replyMsg = if (response.isSuccessful && body?.success == true && !body.orders.isNullOrEmpty()) {
                                                    val order = body.orders.first()
                                                    "Order $orderId status is: ${order.status}. Date: ${order.createdAt}. Total: $${order.totalAmount}."
                                                } else {
                                                    "Sorry, I could not find an order with ID $orderId."
                                                }
                                                // Switch to main to update ui state safely
                                                withContext(Dispatchers.Main) {
                                                    messages.add(ChatMessage(replyMsg, false))
                                                }
                                            } catch(e: Exception) {
                                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                    messages.add(ChatMessage("Sorry, an error occurred while looking up that order.", false))
                                                }
                                            }
                                        }
                                    } catch(e: Exception) {
                                        messages.add(ChatMessage("That doesn't look like a valid order ID.", false))
                                    }
                                    return@IconButton
                                }

                                val keywordMap = mapOf(
                                    listOf("delivery", "shipping") to "delivery_fee",
                                    listOf("cod", "cash") to "cod_enabled",
                                    listOf("hour", "time", "hours") to "working_hours",
                                    listOf("location", "where", "branch", "locations") to "store_locations",
                                    listOf("tip", "laundry", "wash", "tips") to "care_tips"
                                )
                                
                                var matchedReply: String? = null
                                for ((keywords, settingKey) in keywordMap) {
                                    if (keywords.any { lower.contains(it) }) {
                                        val settingValue = settings[settingKey]
                                        if (!settingValue.isNullOrEmpty()) {
                                            matchedReply = settingValue
                                            break
                                        }
                                    }
                                }

                                val reply = matchedReply ?: when {
                                    lower.contains("status") || lower.contains("track") ->
                                        "You can track your order status in the 'Orders' tab of your profile. Or simply send me 'Order ID <number>'."
                                    lower.contains("order") ->
                                        "To view your order details, check the 'Orders' section in your profile. You can also send me 'Order ID <number>'."
                                    lower.contains("return") || lower.contains("refund") ->
                                        "We offer a 30-day return policy for unused items in their original packaging."
                                    lower.contains("payment") || lower.contains("card") ->
                                        "We accept COD, major credit cards, and digital wallets for your convenience."
                                    lower.contains("discount") || lower.contains("promo") ->
                                        "Keep an eye on our app for special promotions! Join the loyalty club for exclusive discounts."
                                    else ->
                                        "I am your virtual assistant! You can ask me about delivery fees, COD, returns, locations, working hours, order tracking, or call our support at ${settings["support_phone"] ?: "1-800-SNOWWHITE"}."
                                }
                                messages.add(ChatMessage(reply, false))
                            }
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                            .size(48.dp)
                    ) {
                        Icon(Icons.Default.Send, "Send", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val align = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bg = if (msg.isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (msg.isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = align
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (msg.isUser) 16.dp else 0.dp,
                bottomEnd = if (msg.isUser) 0.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(containerColor = bg),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = msg.text,
                modifier = Modifier.padding(12.dp),
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
