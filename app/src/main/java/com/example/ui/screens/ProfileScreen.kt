package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.UserProfile
import com.example.viewmodel.MarketViewModel

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

@Composable
fun ProfileScreen(
    viewModel: MarketViewModel,
    onNavigateToTab: (Int) -> Unit
) {
    val loggedInUser by viewModel.loggedInUser.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember { com.example.data.SessionManager(context) }

    var isCreatingState by remember { mutableStateOf(false) } // toggle between login & sign-up forms

    // Input States
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }
    var forgotPasswordResult by remember { mutableStateOf<String?>(null) }
    
    var fullNameVal by remember { mutableStateOf("") }
    var emailVal by remember { mutableStateOf("") }
    var phoneVal by remember { mutableStateOf("") }
    var cityVal by remember { mutableStateOf("Karachi") }
    var addressVal by remember { mutableStateOf("") }
    var passwordVal by remember { mutableStateOf("") }

    var formError by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }

    val showBiometricAuth = {
        val activity = context as? FragmentActivity
        if (activity != null) {
            val executor = ContextCompat.getMainExecutor(activity)
            val biometricPrompt = BiometricPrompt(activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        val cachedEmail = sessionManager.getCachedEmail()
                        val cachedName = sessionManager.getCachedName()
                        val cachedRole = sessionManager.getCachedRole()
                        if (cachedEmail != null && cachedName != null && cachedRole != null) {
                            viewModel.autoLoginFromCache(cachedEmail, cachedName, cachedRole)
                            successMsg = "Biometric Login Successful"
                            onNavigateToTab(0)
                        }
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        formError = "Biometric Auth Error: $errString"
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Login")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Cancel")
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
    }

    LaunchedEffect(Unit) {
        if (loggedInUser == null && sessionManager.isBiometricEnabled() && sessionManager.getCachedEmail() != null) {
            showBiometricAuth()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Decorative Premium Header Cover
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile cover icon",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (loggedInUser != null) "MEMBER REGISTRY" else "SNOWHITE MEMBERSHIP",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content Area depending on authenticated state
        val user = loggedInUser
        val allOrders by viewModel.ordersState.collectAsState()
        if (user != null) {
            UserProfileCard(
                user = user,
                allOrders = allOrders ?: emptyList(),
                viewModel = viewModel,
                onLogout = { 
                    viewModel.logout() 
                    onNavigateToTab(4)
                },
                onUpdatePreferences = { newPrefs ->
                    viewModel.updateSavedPreferences(newPrefs)
                },
                onNavigateToTab = onNavigateToTab
            )
        } else {
            // Unauthenticated view showing beautiful Sign Up / Sign In Forms
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("account_register_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (isCreatingState) "Create Premium Account" else "Sign In to Your Account",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isCreatingState) "Join Snowhite Loyalty Club to earn 150 welcome points instantly!" else "Access orders tracking, store settings, and saved locations.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    if (formError != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = formError ?: "",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                val err = formError ?: ""
                                if (err.contains("Server") || err.contains("HTTP 5") || err.contains("network", ignoreCase=true) || err.contains("timeout", ignoreCase=true) || err.contains("failed", ignoreCase=true) || err.contains("Exception", ignoreCase=true) || err.contains("reach", ignoreCase=true)) {
                                    TextButton(
                                        onClick = {
                                            if (isCreatingState) {
                                                viewModel.createAccount(
                                                    fullName = fullNameVal,
                                                    email = emailVal,
                                                    phone = phoneVal,
                                                    city = cityVal,
                                                    address = addressVal,
                                                    passwordEntered = passwordVal
                                                ) { success, errMsg ->
                                                    if (success) {
                                                        successMsg = "Premium Card Account created successfully! Logged in as Member."
                                                        formError = null
                                                        if (sessionManager.isBiometricEnabled()) {
                                                            sessionManager.cacheSecureSession(emailVal, fullNameVal, "customer")
                                                        }
                                                        if (viewModel.loggedInUser.value?.isAdmin == true) {
                                                            onNavigateToTab(11)
                                                        } else {
                                                            onNavigateToTab(0)
                                                        }
                                                    } else {
                                                        formError = errMsg
                                                        successMsg = null
                                                        android.widget.Toast.makeText(context, errMsg ?: "Account creation failed.", android.widget.Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            } else {
                                                viewModel.login(emailVal, passwordVal) { success, errMsg ->
                                                    if (success) {
                                                        successMsg = "Successfully authenticated. Welcome back!"
                                                        formError = null
                                                        if (sessionManager.isBiometricEnabled()) {
                                                            val uname = emailVal.substringBefore("@")
                                                            sessionManager.cacheSecureSession(emailVal, uname, "customer")
                                                        }
                                                        if (viewModel.loggedInUser.value?.isAdmin == true) {
                                                            onNavigateToTab(11)
                                                        } else {
                                                            onNavigateToTab(0)
                                                        }
                                                    } else {
                                                        formError = errMsg ?: "Could not verify profile credentials."
                                                        successMsg = null
                                                        android.widget.Toast.makeText(context, formError, android.widget.Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            }
                                        }
                                    ) {
                                        Text("Retry", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    if (successMsg != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = successMsg ?: "",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // Form Fields
                    if (isCreatingState) {
                        OutlinedTextField(
                            value = fullNameVal,
                            onValueChange = { fullNameVal = it; formError = null },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Badge, "Name icon") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("register_fullname_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = emailVal,
                        onValueChange = { emailVal = it; formError = null },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, "Email icon") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("register_email_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (isCreatingState) {
                        OutlinedTextField(
                            value = phoneVal,
                            onValueChange = { phoneVal = it; formError = null },
                            label = { Text("Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, "Phone icon") },
                            placeholder = { Text("e.g. 03001234567") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("register_phone_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // City selector dropdown simulated
                        Text(
                            text = "Primary Delivery City",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Karachi", "Lahore", "Islamabad").forEach { city ->
                                val isSel = cityVal == city
                                FilterChip(
                                    selected = isSel,
                                    onClick = { cityVal = city },
                                    label = { Text(city) },
                                    modifier = Modifier.testTag("city_chip_$city"),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = addressVal,
                            onValueChange = { addressVal = it; formError = null },
                            label = { Text("Delivery Shipping Address") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, "Address icon") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("register_address_input"),
                            singleLine = false,
                            minLines = 2,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = passwordVal,
                        onValueChange = { passwordVal = it; formError = null },
                        label = { Text("Security Password (min 6 chars)") },
                        leadingIcon = { Icon(Icons.Default.Lock, "Lock icon") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("register_password_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (emailVal.isBlank() || passwordVal.isBlank()) {
                                formError = "Please fill in email and password fields."
                                return@Button
                            }
                            if (passwordVal.trim().length < 6) {
                                formError = "Identity validation requires password length of 6 characters minimum."
                                return@Button
                            }
                            if (isCreatingState) {
                                if (fullNameVal.isBlank() || phoneVal.isBlank() || addressVal.isBlank()) {
                                    formError = "Please complete all registration fields."
                                    return@Button
                                }
                                viewModel.createAccount(
                                    fullName = fullNameVal,
                                    email = emailVal,
                                    phone = phoneVal,
                                    city = cityVal,
                                    address = addressVal,
                                    passwordEntered = passwordVal
                                ) { success, errMsg ->
                                    if (success) {
                                        successMsg = "Premium Card Account created successfully! Logged in as Member."
                                        formError = null
                                        if (sessionManager.isBiometricEnabled()) {
                                            sessionManager.cacheSecureSession(emailVal, fullNameVal, "customer")
                                        }
                                        if (viewModel.loggedInUser.value?.isAdmin == true) {
                                            onNavigateToTab(11)
                                        } else {
                                            onNavigateToTab(0)
                                        }
                                    } else {
                                        formError = errMsg
                                        successMsg = null
                                        android.widget.Toast.makeText(context, errMsg ?: "Account creation failed.", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                viewModel.login(emailVal, passwordVal) { success, errMsg ->
                                    if (success) {
                                        successMsg = "Successfully authenticated. Welcome back!"
                                        formError = null
                                        if (sessionManager.isBiometricEnabled()) {
                                            val uname = emailVal.substringBefore("@")
                                            sessionManager.cacheSecureSession(emailVal, uname, "customer")
                                        }
                                        if (viewModel.loggedInUser.value?.isAdmin == true) {
                                            onNavigateToTab(11)
                                        } else {
                                            onNavigateToTab(0)
                                        }
                                    } else {
                                        formError = errMsg ?: "Could not verify profile credentials."
                                        successMsg = null
                                        android.widget.Toast.makeText(context, formError, android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_account_form_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isCreatingState) "Register Account & Save" else "Authentication Secure Sign In",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = {
                            isCreatingState = !isCreatingState
                            formError = null
                            successMsg = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isCreatingState) "Already registered? Sign In instead" else "New shopper? Create custom membership index",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    if (!isCreatingState) {
                        TextButton(
                            onClick = {
                                showForgotPasswordDialog = true
                                forgotPasswordEmail = emailVal
                                forgotPasswordResult = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Forgot Password?",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (!isCreatingState && sessionManager.isBiometricEnabled() && sessionManager.getCachedEmail() != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = showBiometricAuth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Fingerprint, "Biometric icon", modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Log In with Biometrics", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        
        if (showForgotPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showForgotPasswordDialog = false },
                title = { Text("Reset Password") },
                text = {
                    Column {
                        Text("Enter your email address to receive a password reset link.")
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = forgotPasswordEmail,
                            onValueChange = { forgotPasswordEmail = it },
                            label = { Text("Email") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (forgotPasswordResult != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(forgotPasswordResult!!, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (forgotPasswordEmail.isNotBlank()) {
                            viewModel.forgotPassword(forgotPasswordEmail) { success, msg ->
                                forgotPasswordResult = msg
                                if (success) {
                                    android.widget.Toast.makeText(context, "Reset instructions sent.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }) {
                        Text("Reset")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Section: Store Settings & Branch Locations in Pakistan
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = " Snowhite Branch Service Centers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                BranchLocationItem(
                    city = "Karachi Head Office",
                    location = "12-C, Commercial Lane, Phase II Extension, DHA",
                    phone = "(021) 111-766-944"
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                BranchLocationItem(
                    city = "Lahore Boutique",
                    location = "Shop 14, Ground Floor, Pace Mall, Gulberg III",
                    phone = "(042) 3575-1122"
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                BranchLocationItem(
                    city = "Islamabad Express Hub",
                    location = "Block 8-B, Sector F-6 Super Market",
                    phone = "(051) 282-5566"
                )
            }
        }

        // Section: About Snowhite PK
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "About Snowhite Pakistan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Established with a legacy of absolute purity and premium care, Snowhite is Pakistan's iconic luxury e-commerce department store and high-class fabric care specialist.\n\nFrom exquisite international cosmetics formulations, premium French and oriental oud fragrances, to luxury intima apparel collections and state-of-the-art dry cleaning technologies, we provide a sophisticated premium lifestyle gateway for Pakistani shoppers.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "App Version: 3.5.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "snowhite.pk",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun UserProfileCard(
    user: UserProfile,
    allOrders: List<com.example.data.Order>,
    viewModel: MarketViewModel,
    onLogout: () -> Unit,
    onUpdatePreferences: (String) -> Unit,
    onNavigateToTab: (Int) -> Unit
) {
    var selectedReceiptOrder by remember { mutableStateOf<com.example.data.Order?>(null) }
    val savedAddresses by viewModel.savedAddresses.collectAsState()
    var isAddingAddress by remember { mutableStateOf(false) }

    var newAddressTitle by remember { mutableStateOf("") }
    var newAddressStr by remember { mutableStateOf("") }
    var newAddressPhone by remember { mutableStateOf("") }
    
    val context = androidx.compose.ui.platform.LocalContext.current

    if (isAddingAddress) {
        AlertDialog(
            onDismissRequest = { isAddingAddress = false },
            title = { Text("Add Saved Address") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newAddressTitle,
                        onValueChange = { newAddressTitle = it },
                        label = { Text("Title (e.g., Home)") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = newAddressStr,
                        onValueChange = { newAddressStr = it },
                        label = { Text("Full Address") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = newAddressPhone,
                        onValueChange = { newAddressPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newAddressTitle.isNotBlank() && newAddressStr.isNotBlank()) {
                        viewModel.addSavedAddress(newAddressTitle, newAddressStr, newAddressPhone)
                        isAddingAddress = false
                        newAddressTitle = ""
                        newAddressStr = ""
                        newAddressPhone = ""
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { isAddingAddress = false }) { Text("Cancel") }
            }
        )
    }

    selectedReceiptOrder?.let { receiptOrder ->
        ReceiptDetailDialog(order = receiptOrder, onDismiss = { selectedReceiptOrder = null })
    }

    // Saved Addresses Section
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Addresses",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Saved Addresses",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                TextButton(onClick = { isAddingAddress = true }) {
                    Text("Add")
                }
            }

            if (savedAddresses.isEmpty()) {
                Text(
                    text = "No saved addresses. Add one for faster checkout.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                savedAddresses.forEach { addr ->
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        Text(
                            text = addr.title,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = addr.fullAddress,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = addr.phoneNumber,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Divider(modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("user_profile_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "VIP Member Profile",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Points star",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${user.membershipPoints} Points",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Info Listing Table
            ProfileDataRow(label = "Primary Email", value = user.email, icon = Icons.Default.Email)
            ProfileDataRow(label = "Phone Contact", value = user.phoneNumber, icon = Icons.Default.Phone)
            ProfileDataRow(label = "Membership Region", value = user.city, icon = Icons.Default.LocationCity)
            ProfileDataRow(label = "Delivery Destination", value = user.deliveryAddress, icon = Icons.Default.HomeWork)

            Spacer(modifier = Modifier.height(12.dp))
            var isEditingProfile by remember { mutableStateOf(false) }
            
            OutlinedButton(
                onClick = { isEditingProfile = true },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Edit, "Edit Profile", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile Details", fontWeight = FontWeight.Bold)
            }
            
            if (isEditingProfile) {
                var editName by remember { mutableStateOf(user.fullName) }
                var editEmail by remember { mutableStateOf(user.email) }
                var editPhone by remember { mutableStateOf(user.phoneNumber) }
                var isSavingProfile by remember { mutableStateOf(false) }
                
                AlertDialog(
                    onDismissRequest = { if (!isSavingProfile) isEditingProfile = false },
                    title = { Text("Edit Profile") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Full Name") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = editEmail,
                                onValueChange = { editEmail = it },
                                label = { Text("Email") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = editPhone,
                                onValueChange = { editPhone = it },
                                label = { Text("Phone Number") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (editName.isNotBlank() && editEmail.isNotBlank() && editPhone.isNotBlank()) {
                                    isSavingProfile = true
                                    val sessionManager = com.example.data.SessionManager(context)
                                    val session = sessionManager.fetchSession()
                                    if (session != null) {
                                        viewModel.updateUserProfile(session.userId, user.email, editEmail, editName, editPhone) { success, msg ->
                                            isSavingProfile = false
                                            if (success) {
                                                isEditingProfile = false
                                            } else {
                                                // Could show toast or error msg
                                            }
                                        }
                                    } else {
                                        isSavingProfile = false
                                        isEditingProfile = false
                                    }
                                }
                            },
                            enabled = !isSavingProfile
                        ) {
                            if (isSavingProfile) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            } else {
                                Text("Save")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { isEditingProfile = false },
                            enabled = !isSavingProfile
                        ) { Text("Cancel") }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "SECURITY SETTINGS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            
            val sessionManager = remember { com.example.data.SessionManager(context) }
            var biometricEnabled by remember { mutableStateOf(sessionManager.isBiometricEnabled()) }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Biometric Login",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Use fingerprint or face unlock to sign in",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = biometricEnabled,
                    onCheckedChange = {
                        biometricEnabled = it
                        sessionManager.setBiometricEnabled(it)
                        if (it) {
                            sessionManager.cacheSecureSession(user.email, user.fullName, if (user.isAdmin) "admin" else "customer")
                        }
                    }
                )
            }
            
            // App Notifications Settings
            val contextForPrefs = androidx.compose.ui.platform.LocalContext.current
            val sharedPrefsForNotifications = remember { contextForPrefs.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE) }
            
            var notificationsEnabled by remember { mutableStateOf(sharedPrefsForNotifications.getBoolean("notifications_enabled", true)) }
            var soundEnabled by remember { mutableStateOf(sharedPrefsForNotifications.getBoolean("notification_sound_enabled", true)) }
            var soundPreset by remember { mutableStateOf(sharedPrefsForNotifications.getString("notification_sound_preset", "Default") ?: "Default") }
            var showPresetsMenu by remember { mutableStateOf(false) }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Push Notifications",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Receive alerts for order status & promos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { 
                        notificationsEnabled = it 
                        sharedPrefsForNotifications.edit().putBoolean("notifications_enabled", it).apply()
                    }
                )
            }
            
            if (notificationsEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Sound", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { 
                            soundEnabled = it
                            sharedPrefsForNotifications.edit().putBoolean("notification_sound_enabled", it).apply()
                        }
                    )
                }
                
                if (soundEnabled) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Notification Tone", style = MaterialTheme.typography.bodyMedium)
                        Box {
                            TextButton(onClick = { showPresetsMenu = true }) {
                                Text(soundPreset)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select tone")
                            }
                            DropdownMenu(
                                expanded = showPresetsMenu,
                                onDismissRequest = { showPresetsMenu = false }
                            ) {
                                listOf("Default", "Digital", "Chime", "Bell").forEach { preset ->
                                    DropdownMenuItem(
                                        text = { Text(preset) },
                                        onClick = {
                                            soundPreset = preset
                                            sharedPrefsForNotifications.edit().putString("notification_sound_preset", preset).apply()
                                            showPresetsMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTab(16) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notification Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Notification Settings",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Manage chat custom ringtones and sounds",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            if (!user.isAdmin) {
                // Help & Support Button (Help Center)
                OutlinedButton(
                    onClick = { onNavigateToTab(5) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(androidx.compose.material.icons.Icons.Default.HelpOutline, "Help center", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("FAQ & Help Center", fontWeight = FontWeight.Bold)
                }
                
                // Chat Support Button
                OutlinedButton(
                    onClick = { onNavigateToTab(5) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(androidx.compose.material.icons.Icons.Default.Chat, "Chat Support", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chat with Admin / Support", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "SAVED PREFERENCES",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Text(
                text = "Tap categories to customize service alerts:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            val currentPrefs = user.savedPreferences.split(",").filter { it.isNotBlank() }
            val categories = listOf("Dry Cleaning", "Laundry", "Carpet & Rugs", "Specialized")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isFav = currentPrefs.contains(category)
                    FilterChip(
                        selected = isFav,
                        onClick = {
                            val newList = currentPrefs.toMutableList()
                            if (isFav) newList.remove(category) else newList.add(category)
                            onUpdatePreferences(newList.joinToString(","))
                        },
                        label = { Text(category) },
                        leadingIcon = if (isFav) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "PERSONAL PURCHASE HISTORY",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            val userOrders = allOrders.filter { order ->
                user.purchaseHistory.split(",").contains(order.id)
            }

            if (userOrders.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "No purchase receipts stored on this profile. Complete checkout payments to record history.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                Text(
                    text = "${userOrders.size} verified transactions recorded securely on Cloud:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                userOrders.forEach { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedReceiptOrder = order },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = order.id,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Rs. ${String.format("%.2f", order.totalAmount)}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = order.itemsSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Status: ${order.status}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (order.status == "Delivered") Color(0xFF4CAF50) else Color(0xFFFF9800)
                                )
                                Text(
                                    text = "To: ${order.shippingAddress}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            if (user.isAdmin) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onNavigateToTab(10) }, // Admin tab or panel
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("admin_dashboard_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688)), // Match brand color
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Admin icon",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Open Admin Dashboard",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("logout_account_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Sign out icon",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign Out & Lock Registry",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun ProfileDataRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun BranchLocationItem(city: String, location: String, phone: String) {
    Column {
        Text(
            text = city,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = location,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Phone call icon",
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = phone,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ReceiptDetailDialog(
    order: com.example.data.Order,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Brand
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "Receipt Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SNOWHITE SERVICES",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "Official Premium Care Receipt",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                Divider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Info Rows
                ReceiptRow(label = "Transaction ID", value = order.id, isMono = true)
                val formattedDate = java.text.SimpleDateFormat("MMM dd, yyyy - hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(order.timestamp))
                ReceiptRow(label = "Date & Time Issued", value = formattedDate)
                ReceiptRow(
                    label = "Payment Status",
                    value = if (order.paymentCardLast4.isEmpty()) "Cash on Delivery" else "Paid via Card (**** ${order.paymentCardLast4})"
                )

                Divider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STATUS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        color = when (order.status) {
                            "Completed", "Delivered" -> Color(0xFFE8F5E9)
                            "Processing" -> Color(0xFFFFF3E0)
                            else -> Color(0xFFE3F2FD)
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = order.status.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = when (order.status) {
                                "Completed", "Delivered" -> Color(0xFF2E7D32)
                                "Processing" -> Color(0xFFE65100)
                                else -> Color(0xFF1565C0)
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Items Breakdown block
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "SERVICE / ORDER DETAIL SUMMARY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Parse the items summary
                        val lines = order.itemsSummary.split("\n", ", ")
                        lines.forEach { line ->
                            // Support "2x Title" and "Title x2"
                            val cleanLine = line.trim()
                            val prefixQuantityMatcher = Regex("^(\\d+)x\\s+(.*)").matchEntire(cleanLine)
                            val title: String
                            val qty: String
                            
                            if (prefixQuantityMatcher != null) {
                                qty = prefixQuantityMatcher.groupValues[1]
                                title = prefixQuantityMatcher.groupValues[2]
                            } else {
                                val parts = cleanLine.split(" x")
                                title = parts.getOrNull(0) ?: cleanLine
                                qty = parts.getOrNull(1) ?: "1"
                            }
                            
                            if (cleanLine.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "x$qty",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Divider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "TOTAL CHARGES (incl. Taxes)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Rs. ${String.format("%.2f", order.totalAmount)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Shipping Schedule
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AirportShuttle,
                                contentDescription = "Delivery Truck",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Logistic Routing Schedule",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Destination Address: ${order.shippingAddress}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (order.pickupSchedule.isNotBlank()) {
                            Text(
                                text = "Pickup Slot: ${order.pickupSchedule}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (order.deliverySchedule.isNotBlank()) {
                            Text(
                                text = "Delivery Slot: ${order.deliverySchedule}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                if (order.status.lowercase() == "delivered") {
                    var showReviewSection by remember { mutableStateOf(false) }
                    var rating by remember { mutableStateOf(5) }
                    var comment by remember { mutableStateOf("") }
                    var isReviewSubmitting by remember { mutableStateOf(false) }
                    var reviewResMsg by remember { mutableStateOf<String?>(null) }
                    val scope = rememberCoroutineScope()

                    if (!showReviewSection) {
                        OutlinedButton(
                            onClick = { showReviewSection = true },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Icon(Icons.Default.Star, "Rate")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Rate & Review this Order")
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Rate your experience", fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                    repeat(5) { i ->
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = "Star",
                                            tint = if (i < rating) MaterialTheme.colorScheme.primary else Color.Gray,
                                            modifier = Modifier.size(32.dp).clickable { rating = i + 1 }
                                        )
                                    }
                                }
                                OutlinedTextField(
                                    value = comment,
                                    onValueChange = { comment = it },
                                    label = { Text("Write your review") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                if (reviewResMsg != null) {
                                    Text(reviewResMsg!!, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                Button(
                                    onClick = {
                                        isReviewSubmitting = true
                                        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                            try {
                                                val req = com.example.network.ReviewRequest(orderId = order.id, productId = 0, rating = rating, comment = comment)
                                                val res = com.example.network.RetrofitClient.apiService.submitReview(req)
                                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                    if (res.isSuccessful && res.body()?.success == true) {
                                                        reviewResMsg = "Review submitted successfully!"
                                                    } else {
                                                        reviewResMsg = "Failed to submit review."
                                                    }
                                                    isReviewSubmitting = false
                                                }
                                            } catch (e: Exception) {
                                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                    reviewResMsg = "Error connecting to server."
                                                    isReviewSubmitting = false
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !isReviewSubmitting
                                ) {
                                    if(isReviewSubmitting) CircularProgressIndicator(modifier=Modifier.size(20.dp)) else Text("Submit Review")
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close Receipt Summary")
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String, isMono: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = if (isMono) androidx.compose.ui.text.font.FontFamily.Monospace else null
        )
    }
}
