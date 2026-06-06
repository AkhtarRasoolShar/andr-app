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
    var isLiveWithAdmin by remember { mutableStateOf(false) }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    // Init welcome message purely client-side
    LaunchedEffect(Unit) {
        messages.add(ChatMessage("Hello! I am your virtual assistant. How can I help you today?", false))
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(isLiveWithAdmin) {
        val user = viewModel.loggedInUser.value
        if (isLiveWithAdmin && user != null && user.id > 0) {
            while (true) {
                try {
                    val apiMsgs = viewModel.getChatHistory(user.id, 1) // admin id = 1
                    val newMsgs = apiMsgs.map { networkMsg ->
                        ChatMessage(
                            text = networkMsg.message,
                            isUser = networkMsg.senderId == user.id
                        )
                    }
                    if (newMsgs.isNotEmpty()) {
                        // Only add messages we don't already have (very basic unique check for UI)
                        withContext(Dispatchers.Main) {
                            messages.clear()
                            messages.addAll(newMsgs)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                kotlinx.coroutines.delay(3000)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isLiveWithAdmin) "Live Admin Chat" else "Virtual Support") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    Text("Human")
                    Switch(
                        checked = isLiveWithAdmin,
                        onCheckedChange = { 
                            isLiveWithAdmin = it 
                            val statusMsg = if (it) "Connecting you to a live human admin..." else "Switched back to virtual assistant."
                            messages.add(ChatMessage(statusMsg, false))
                        }
                    )
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
                state = listState,
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
                                                    "Order $orderId status is: ${order.status}. Date: ${order.createdAt}. Total: Rs. ${order.totalAmount}."
                                                } else {
                                                    "Maazrat, humein is ID $orderId ka order nahi mila."
                                                }
                                                // Switch to main to update ui state safely
                                                withContext(Dispatchers.Main) {
                                                    messages.add(ChatMessage(replyMsg, false))
                                                }
                                            } catch(e: Exception) {
                                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                    messages.add(ChatMessage("Maazrat, order check karte waqt masla paish aaya.", false))
                                                }
                                            }
                                        }
                                    } catch(e: Exception) {
                                        messages.add(ChatMessage("Yeh order ID theek nahi lag rahi.", false))
                                    }
                                    return@IconButton
                                }

                                val keywordMap = mapOf(
                                    listOf("delivery", "shipping", "deliver") to "delivery_fee",
                                    listOf("cod", "cash") to "cod_enabled",
                                    listOf("hour", "time", "hours", "waqt") to "working_hours",
                                    listOf("location", "where", "branch", "locations", "kahan") to "store_locations",
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

                                    if (isLiveWithAdmin) {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            val senderId = viewModel.loggedInUser.value?.id ?: 0
                                            if (senderId > 0) {
                                                val success = viewModel.sendChatMessage(senderId, 1, userText) // Assumes Admin ID = 1
                                                // After sending, we fetch from API in the polling loop
                                            }
                                        }
                                    } else if (matchedReply != null) {
                                        messages.add(ChatMessage(matchedReply, false))
                                    } else {
                                        coroutineScope.launch(Dispatchers.Main) {
                                            messages.add(ChatMessage(if (isLiveWithAdmin) "Admin is typing..." else "Typing...", false))
                                            withContext(Dispatchers.IO) {
                                                try {
                                                    val prompt = "You are a helpful customer support assistant for SnowWhite Boutique. The user says: \"$userText\". Context details: Delivery Fee Rs. ${settings["delivery_fee"] ?: "10"}, App Name: ${settings["app_name"] ?: "SnowWhite Boutique"}. Please reply naturally, briefly, and warmly."
                                                    
                                                    val request = com.example.network.GenerateContentRequest(
                                                        contents = listOf(com.example.network.Content(
                                                            parts = listOf(com.example.network.Part(text = prompt))
                                                        ))
                                                    )
                                                    val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                                                    val response = com.example.network.GeminiRetrofitClient.service.generateContent(apiKey, request)
                                                    val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                                                        ?: "Me ek virtual assistant hu! Aap mujhse delivery, timing, branches, payment, ya order status ke baray me pooch sakte hain, ya humein call karein: ${settings["support_phone"] ?: "1-800-SNOWWHITE"}."
                                                    
                                                    withContext(Dispatchers.Main) {
                                                        messages.removeAt(messages.size - 1) // Remove "Typing..."
                                                        messages.add(ChatMessage(reply, false))
                                                    }
                                                } catch (e: Exception) {
                                                    val fallback = "Me ek virtual assistant hu! Aap mujhse delivery, timing, branches, payment, ya order status ke baray me pooch sakte hain, ya humein call karein: ${settings["support_phone"] ?: "1-800-SNOWWHITE"}."
                                                    withContext(Dispatchers.Main) {
                                                        messages.removeAt(messages.size - 1)
                                                        messages.add(ChatMessage(fallback, false))
                                                    }
                                                }
                                            }
                                        }
                                    }
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
