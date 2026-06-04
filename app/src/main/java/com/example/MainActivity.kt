package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.InventoryRepository
import com.example.ui.screens.AdminInventoryScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.MainCatalogScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MarketViewModel
import com.example.viewmodel.MarketViewModelFactory

import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val requestPermissionLauncher = registerForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                } else {
                    // Inform user that that your app will not show notifications.
                }
            }
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        // Setup local offline database and repository references
        val database = AppDatabase.getDatabase(applicationContext)
        com.example.data.FirestoreService.initialize(applicationContext, database.marketplaceDao())
        com.example.data.FirebaseAuthService.initialize(applicationContext)
        val repository = InventoryRepository(database.marketplaceDao(), applicationContext)
        
        // Setup state viewModel using a custom factory
        val viewModel = ViewModelProvider(
            this,
            MarketViewModelFactory(application, repository)
        )[MarketViewModel::class.java]

        enableEdgeToEdge()
        
         setContent {
             MyApplicationTheme(darkTheme = viewModel.darkModeEnabled) {
                 val sessionManager = remember { com.example.data.SessionManager(applicationContext) }
                  val sharedPrefs = remember {
                     applicationContext.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE) // legacy
                 }
                 val initialTab = remember {
                     val cachedId = sessionManager.fetchSession()?.email
                     val cachedRole = sharedPrefs.getString("role", null)
                     if (cachedId != null) {
                         if ((sessionManager.fetchSession()?.role.equals("admin", ignoreCase = true) || sessionManager.fetchSession()?.role.equals("super_admin", ignoreCase = true))) 2 else 0
                     } else {
                         4 // Start on Login Screen (ProfileScreen)
                     }
                 }
                 var selectedTab by remember { mutableStateOf(initialTab) }
                 val loggedInUser by viewModel.loggedInUser.collectAsState()
                 val cartSummary by viewModel.cartSummary.collectAsState()

                 LaunchedEffect(Unit) {
                     val cachedEmail = sessionManager.fetchSession()?.email
                     val cachedName = sessionManager.fetchSession()?.name
                     val cachedRole = sharedPrefs.getString("role", null)
                     if (cachedEmail != null) {
                         viewModel.autoLoginFromCache(cachedEmail, cachedName ?: "", sessionManager.fetchSession()?.role ?: "")
                     }
                 }

                 LaunchedEffect(loggedInUser) {
                     if (loggedInUser == null) { selectedTab = 4 } else if (loggedInUser?.isAdmin != true && selectedTab == 2) {
                         selectedTab = 0
                     }
                 }

                 Scaffold(
                     modifier = Modifier.fillMaxSize(),
                     floatingActionButton = {
                         FloatingActionButton(
                             onClick = { selectedTab = 5 },
                             containerColor = MaterialTheme.colorScheme.primaryContainer,
                             contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                             modifier = Modifier.testTag("fab_support")
                         ) {
                             Icon(imageVector = androidx.compose.material.icons.Icons.Default.Person, contentDescription = "Virtual Support")
                         }
                     },
                     bottomBar = {
                         NavigationBar(
                             modifier = Modifier
                                 .windowInsetsPadding(WindowInsets.navigationBars)
                                 .testTag("main_bottom_nav"),
                             containerColor = MaterialTheme.colorScheme.surface,
                             tonalElevation = NavigationBarDefaults.Elevation // soft tonal contrast
                         ) {
                             NavigationBarItem(
                                 selected = selectedTab == 0,
                                 onClick = { selectedTab = 0 },
                                 label = { Text("Shop") },
                                 icon = {
                                     Icon(
                                         imageVector = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                                         contentDescription = "Explore Snowhite Boutique Catalog"
                                     )
                                 },
                                 modifier = Modifier.testTag("tab_shop")
                             )

                             NavigationBarItem(
                                 selected = selectedTab == 1,
                                 onClick = { selectedTab = 1 },
                                 label = { Text("Bag") },
                                 icon = {
                                     val qtyCount = cartSummary.items.sumOf { it.cartItem.quantity }
                                     BadgedBox(
                                         badge = {
                                             if (qtyCount > 0) {
                                                 Badge(modifier = Modifier.testTag("cart_badge_count")) {
                                                     Text(text = "$qtyCount")
                                                 }
                                             }
                                         }
                                     ) {
                                         Icon(
                                             imageVector = if (selectedTab == 1) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart,
                                             contentDescription = "Shopping Bag and Checkout"
                                         )
                                     }
                                 },
                                 modifier = Modifier.testTag("tab_cart")
                             )

                             NavigationBarItem(
                                 selected = selectedTab == 3,
                                 onClick = { selectedTab = 3 },
                                 label = { Text("Orders") },
                                 icon = {
                                     Icon(
                                         imageVector = if (selectedTab == 3) Icons.Filled.Receipt else Icons.Outlined.Receipt,
                                         contentDescription = "Order tracking history"
                                     )
                                 },
                                 modifier = Modifier.testTag("tab_orders")
                             )

                             NavigationBarItem(
                                 selected = selectedTab == 4,
                                 onClick = { selectedTab = 4 },
                                 label = { Text("Account") },
                                 icon = {
                                     Icon(
                                         imageVector = if (selectedTab == 4) Icons.Filled.Person else Icons.Outlined.Person,
                                         contentDescription = "User loyalty account and settings"
                                     )
                                 },
                                 modifier = Modifier.testTag("tab_profile")
                             )

                             if (loggedInUser?.isAdmin == true) {
                                 NavigationBarItem(
                                     selected = selectedTab == 2,
                                     onClick = { selectedTab = 2 },
                                     label = { Text("Portal") },
                                     icon = {
                                         Icon(
                                             imageVector = if (selectedTab == 2) Icons.Filled.Build else Icons.Outlined.Build,
                                             contentDescription = "Merchant inventory controls"
                                         )
                                     },
                                     modifier = Modifier.testTag("tab_admin")
                                 )
                             }
                         }
                     }
                 ) { innerPadding ->
                    Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                        when (selectedTab) {
                            0 -> MainCatalogScreen(
                                viewModel = viewModel,
                                onNavigateToTab = { targetTab -> selectedTab = targetTab }
                            )
                            1 -> CartScreen(
                                viewModel = viewModel,
                                onNavigateToTab = { targetTab -> selectedTab = targetTab }
                            )
                            2 -> AdminInventoryScreen(viewModel = viewModel)
                            3 -> OrdersScreen(
                                viewModel = viewModel,
                                onNavigateToTab = { targetTab -> selectedTab = targetTab }
                            )
                            4 -> ProfileScreen(
                                viewModel = viewModel,
                                onNavigateToTab = { targetTab -> selectedTab = targetTab }
                            )
                            5 -> com.example.ui.screens.SupportChatScreen(
                                viewModel = viewModel,
                                onBack = { selectedTab = 0 }
                            )
                        }
                    }
                }
            }
        }
    }
}
