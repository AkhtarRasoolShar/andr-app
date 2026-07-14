package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Order
import com.example.data.Product
import com.example.viewmodel.ApiProductState
import com.example.viewmodel.CartSummary
import com.example.viewmodel.CartUiItem
import com.example.viewmodel.MarketViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.ui.theme.*
import androidx.compose.animation.core.*
import androidx.compose.ui.composed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import coil.compose.AsyncImage
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue

fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    val shimmerColors = listOf(
        Color(0xFFE0E0E0),
        Color(0xFFF5F5F5),
        Color(0xFFE0E0E0)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )
    background(brush)
}

@Composable
fun ShimmerProductCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(modifier = Modifier.padding(12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .shimmerEffect()
                    )
                }
            }
        }
    }
}

@Composable
fun AutoScrollingCarousel(
    modifier: Modifier = Modifier
) {
    val promoImages = listOf(
        "https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?auto=format&fit=crop&w=800&q=80",
        "https://images.unsplash.com/photo-1441986300917-64674bd600d8?auto=format&fit=crop&w=800&q=80",
        "https://images.unsplash.com/photo-1540959733332-eab4deabeeaf?auto=format&fit=crop&w=800&q=80",
        "https://images.unsplash.com/photo-1620799140408-edc6dcb6d633?auto=format&fit=crop&w=800&q=80"
    )
    val pagerState = rememberPagerState(pageCount = { promoImages.size })

    LaunchedEffect(pagerState) {
        while (true) {
            kotlinx.coroutines.delay(3000)
            try {
                val nextPage = (pagerState.currentPage + 1) % promoImages.size
                pagerState.animateScrollToPage(nextPage)
            } catch (e: Exception) {
                // Ignore transient swipe cancellation
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .testTag("promo_carousel")
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val img = promoImages[page]
            val finalUrl = if (img.startsWith("http://") || img.startsWith("https://")) img else "https://akhtarhussain.site/api/$img"
            AsyncImage(
                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                    .data(finalUrl)
                    .crossfade(500)
                    .error(android.R.drawable.ic_menu_report_image)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .build(),
                contentDescription = "Promotional Offer ${page + 1}",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray)
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(promoImages.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}

// ==========================================
// PART 1: PROCEDURAL CUSTOM DECORATIVE CANVAS
// Representing hand-crafted items beautifully without relying on faulty web image URLs
// ==========================================

@Composable
fun ProceduralCraftImage(category: String, subkey: String, modifier: Modifier = Modifier) {
    if (subkey.startsWith("http://", ignoreCase = true) || subkey.startsWith("https://", ignoreCase = true)) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            coil.compose.AsyncImage(
                model = subkey,
                contentDescription = "Active fabric care product dynamic view",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }
        return
    }

    val goldCol = Color(0xFFC5A059)
    val darkGold = Color(0xFF9E7E44)
    val blushCol = Color(0xFFE5B5B0)
    val crimsonCol = Color(0xFF8B1E3F)
    val charcoal = Color(0xFF2C2421)
    val glassAqua = Color(0xFF8EBEB5)
    val cloudBlue = Color(0xFF769ECB)
    val whitePearl = Color(0xFFFAFAF5)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(90.dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val cx = canvasWidth / 2
            val cy = canvasHeight / 2

            when (category.lowercase()) {
                "cosmetics" -> {
                    if (subkey.contains("palette")) {
                        // Sleek open makeup palette compact
                        // Palette casing
                        drawRoundRect(
                            color = charcoal,
                            topLeft = Offset(cx * 0.3f, cy * 0.4f),
                            size = Size(cx * 1.4f, cy * 1.2f),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                        // Inner mirror
                        drawRect(
                            color = Color(0xFFE3F2FD),
                            topLeft = Offset(cx * 0.45f, cy * 0.5f),
                            size = Size(cx * 1.1f, cy * 0.4f)
                        )
                        // Rounded make-up color pans (crimson, gold, blush)
                        drawCircle(color = crimsonCol, radius = cx * 0.16f, center = Offset(cx * 0.6f, cy * 1.25f))
                        drawCircle(color = goldCol, radius = cx * 0.16f, center = Offset(cx, cy * 1.25f))
                        drawCircle(color = blushCol, radius = cx * 0.16f, center = Offset(cx * 1.4f, cy * 1.25f))
                    } else {
                        // Elegant upright matte lipstick
                        // Lipstick case base
                        drawRoundRect(
                            color = charcoal,
                            topLeft = Offset(cx * 0.75f, cy * 0.9f),
                            size = Size(cx * 0.5f, cy * 0.7f),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                        // Gold casing ring band
                        drawRect(
                            color = goldCol,
                            topLeft = Offset(cx * 0.75f, cy * 0.82f),
                            size = Size(cx * 0.5f, cy * 0.15f)
                        )
                        // Inner gold tube metal extension
                        drawRoundRect(
                            color = darkGold,
                            topLeft = Offset(cx * 0.83f, cy * 0.55f),
                            size = Size(cx * 0.34f, cy * 0.3f)
                        )
                        // Slashed cylinder Crimson paste lipstick stick
                        val lipstickPath = Path().apply {
                            moveTo(cx * 0.83f, cy * 0.6f)
                            lineTo(cx * 0.83f, cy * 0.25f)
                            lineTo(cx * 1.17f, cy * 0.38f)
                            lineTo(cx * 1.17f, cy * 0.6f)
                            close()
                        }
                        drawPath(path = lipstickPath, color = crimsonCol)
                    }
                }
                "fragrances" -> {
                    // Luxurious French Fragrance Cyrstal Atomizer
                    // Bottle base shape (curvy glass bottle)
                    val bottlePath = Path().apply {
                        moveTo(cx * 0.6f, cy * 0.7f)
                        cubicTo(cx * 0.4f, cy * 0.8f, cx * 0.4f, cy * 1.5f, cx * 0.6f, cy * 1.6f)
                        lineTo(cx * 1.4f, cy * 1.6f)
                        cubicTo(cx * 1.6f, cy * 1.5f, cx * 1.6f, cy * 0.8f, cx * 1.4f, cy * 0.7f)
                        close()
                    }
                    drawPath(path = bottlePath, color = whitePearl)
                    
                    // Golden perfume liquid inside (half full)
                    val liquidPath = Path().apply {
                        moveTo(cx * 0.65f, cy * 1.1f)
                        lineTo(cx * 0.65f, cy * 1.55f)
                        lineTo(cx * 1.35f, cy * 1.55f)
                        lineTo(cx * 1.35f, cy * 1.1f)
                        close()
                    }
                    drawPath(path = liquidPath, color = goldCol.copy(alpha = 0.55f))
                    
                    // Glass bottle details
                    drawPath(path = bottlePath, color = goldCol, style = Stroke(width = 3f))
                    
                    // Neck band
                    drawRect(
                        color = goldCol,
                        topLeft = Offset(cx * 0.85f, cy * 0.55f),
                        size = Size(cx * 0.3f, cy * 0.15f)
                    )
                    // Spray cap assembly on top
                    drawRoundRect(
                        color = darkGold,
                        topLeft = Offset(cx * 0.9f, cy * 0.35f),
                        size = Size(cx * 0.2f, cy * 0.2f),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
                "personal care" -> {
                    if (subkey.contains("serum")) {
                        // Skincare serum glass medicine dropper
                        // Bottle
                        drawRoundRect(
                            color = Color(0x995D4037), // Amber brown glass
                            topLeft = Offset(cx * 0.7f, cy * 0.6f),
                            size = Size(cx * 0.6f, cy * 1.0f),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                            style = Stroke(width = 3f)
                        )
                        // Liquid level
                        drawRoundRect(
                            color = glassAqua.copy(alpha = 0.7f),
                            topLeft = Offset(cx * 0.72f, cy * 0.9f),
                            size = Size(cx * 0.56f, cy * 0.66f),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                        // Collar white plastic screw cap
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(cx * 0.8f, cy * 0.45f),
                            size = Size(cx * 0.4f, cy * 0.18f),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                        // Rubber squeeze bulb topper
                        drawArc(
                            color = Color.Gray,
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = true,
                            size = Size(cx * 0.3f, cy * 0.3f),
                            topLeft = Offset(cx * 0.85f, cy * 0.18f)
                        )
                    } else {
                        // Wide luxury skin moisturizing butter tub
                        // Base container
                        drawRoundRect(
                            color = glassAqua,
                            topLeft = Offset(cx * 0.4f, cy * 0.8f),
                            size = Size(cx * 1.2f, cy * 0.7f),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                        // Shiny lid topper
                        drawRoundRect(
                            color = goldCol,
                            topLeft = Offset(cx * 0.35f, cy * 0.62f),
                            size = Size(cx * 1.3f, cy * 0.22f),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                        // Center brand rose circle
                        drawCircle(color = blushCol, radius = cx * 0.15f, center = Offset(cx, cy * 1.15f))
                    }
                }
                "apparel" -> {
                    if (subkey.contains("camisole")) {
                        // Intimate smooth silk camisole top
                        val path = Path().apply {
                            moveTo(cx * 0.6f, cy * 0.7f)
                            lineTo(cx * 0.7f, cy * 0.5f) // straps start
                            lineTo(cx * 0.72f, cy * 0.5f)
                            lineTo(cx * 0.8f, cy * 0.7f) // strap left end
                            lineTo(cx * 1.2f, cy * 0.7f) // strap right start
                            lineTo(cx * 1.28f, cy * 0.5f)
                            lineTo(cx * 1.3f, cy * 0.5f)
                            lineTo(cx * 1.4f, cy * 0.7f) // bodice top
                            lineTo(cx * 1.45f, cy * 1.5f) // bottom hem right
                            lineTo(cx * 0.55f, cy * 1.5f) // bottom hem left
                            close()
                        }
                        drawPath(path = path, color = blushCol)
                        // Lace highlights
                        drawCircle(color = whitePearl, radius = 5f, center = Offset(cx, cy * 0.8f))
                        drawCircle(color = whitePearl, radius = 5f, center = Offset(cx * 0.85f, cy * 0.75f))
                        drawCircle(color = whitePearl, radius = 5f, center = Offset(cx * 1.15f, cy * 0.75f))
                    } else {
                        // Premium folded Satin PJ button-up suit representation
                        drawRoundRect(
                            color = Color(0xFFDCD6D1),
                            topLeft = Offset(cx * 0.35f, cy * 0.45f),
                            size = Size(cx * 1.3f, cy * 1.1f),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                        // Contrasting piping borders & collar triangles
                        val collarLeft = Path().apply {
                            moveTo(cx * 0.7f, cy * 0.45f)
                            lineTo(cx * 0.95f, cy * 0.85f)
                            lineTo(cx * 0.5f, cy * 0.8f)
                            close()
                        }
                        drawPath(path = collarLeft, color = charcoal)
                        
                        val collarRight = Path().apply {
                            moveTo(cx * 1.3f, cy * 0.45f)
                            lineTo(cx * 1.05f, cy * 0.85f)
                            lineTo(cx * 1.5f, cy * 0.8f)
                            close()
                        }
                        drawPath(path = collarRight, color = charcoal)

                        // Pocket
                        drawRoundRect(
                            color = blushCol,
                            topLeft = Offset(cx * 0.42f, cy * 1.05f),
                            size = Size(cx * 0.35f, cy * 0.4f),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }
                }
                "dry cleaning" -> {
                    // Express premium Dry-Clean clothing ticket coupon voucher
                    // Background coupon
                    drawRoundRect(
                        color = cloudBlue,
                        topLeft = Offset(cx * 0.2f, cy * 0.35f),
                        size = Size(cx * 1.6f, cy * 1.2f),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                    // Dashed lines detailing a secure tear coupon
                    drawLine(
                        color = Color.White,
                        start = Offset(cx * 0.65f, cy * 0.35f),
                        end = Offset(cx * 0.65f, cy * 1.55f),
                        strokeWidth = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                    // Minimalist clothing metal laundry hanger vector illustration
                    val hangerPath = Path().apply {
                        moveTo(cx * 1.2f, cy * 0.85f) // bottom left corner
                        lineTo(cx * 1.5f, cy * 0.85f) // bottom right corner
                        lineTo(cx * 1.35f, cy * 0.62f) // center high apex
                        close()
                    }
                    drawPath(path = hangerPath, color = Color.White, style = Stroke(width = 3.5f))
                    // Hanger upper hook curve
                    drawArc(
                        color = Color.White,
                        startAngle = 0f,
                        sweepAngle = 270f,
                        useCenter = false,
                        size = Size(cx * 0.16f, cy * 0.25f),
                        topLeft = Offset(cx * 1.3f, cy * 0.4f),
                        style = Stroke(width = 3.5f)
                    )
                }
                else -> {
                    drawCircle(color = Color.Gray, radius = cx * 0.4f, center = Offset(cx, cy))
                }
            }
        }
    }
}

// Stride custom helper replacing kotlin.stdlib features to guarantee stability
private fun stride(start: Float, end: Float, step: Float): List<Float> {
    val list = mutableListOf<Float>()
    var curr = start
    while (curr <= end) {
        list.add(curr)
        curr += step
    }
    return list
}


// ==========================================
// PART 2: GENERAL APP LAYOUT & EXPLORE
// ==========================================

@Composable
fun WishlistScreen(viewModel: MarketViewModel, onClose: () -> Unit) {
    LaunchedEffect(Unit) {
        viewModel.loadWishlistFromApi()
    }
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val products by viewModel.productsState.collectAsState()
    val wishlistProducts = products.filter { wishlistIds.contains(it.id) }
    var activeProductForDetail by remember { mutableStateOf<Product?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App top title
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Your Favorites",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (wishlistProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Empty wishlist",
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your wishlist is bare.",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(wishlistProducts, key = { "wishlist" + it.id }) { item ->
                    ProductListingCard(
                        product = item,
                        viewModel = viewModel,
                        onClick = { activeProductForDetail = item },
                        onQuickAdd = { viewModel.addToCart(item) }
                    )
                }
            }
        }
    }

    activeProductForDetail?.let { pd ->
        ProductDetailModal(
            product = pd,
            viewModel = viewModel,
            onDismiss = { activeProductForDetail = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainCatalogScreen(
    viewModel: MarketViewModel,
    onNavigateToTab: (Int) -> Unit
) {
    val products by viewModel.productsState.collectAsState()
    val selectedCat by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val cartSummary by viewModel.cartSummary.collectAsState()
    val loggedInUser by viewModel.loggedInUser.collectAsState()
    val apiState by viewModel.apiState.collectAsState()

    var activeProductForDetail by remember { mutableStateOf<Product?>(null) }
    var isWishlistOpen by remember { mutableStateOf(false) }
    var showAdminCategoryMenu by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val context = LocalContext.current
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val speechText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!speechText.isNullOrEmpty()) {
                viewModel.updateSearchQuery(speechText)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now to search...")
                }
                speechLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Voice input not supported on this device.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Audio permission is required for voice search.", Toast.LENGTH_SHORT).show()
        }
    }

    fun startVoiceSearch() {
        val pm = context.packageManager
        val dummyIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        if (dummyIntent.resolveActivity(pm) == null) {
            Toast.makeText(context, "Voice input not supported on this device.", Toast.LENGTH_SHORT).show()
            return
        }
        permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
    }

    if (isWishlistOpen) {
        WishlistScreen(viewModel = viewModel, onClose = { isWishlistOpen = false })
        return
    }
    
    var showContactSupport by remember { mutableStateOf(false) }
    var showAllProducts by remember { mutableStateOf(false) }

    if (showAllProducts) {
        AllProductsScreen(
            viewModel = viewModel,
            onBack = { showAllProducts = false },
            onProductClick = { activeProductForDetail = it },
            onNavigateToCart = { onNavigateToTab(1) }
        )
        activeProductForDetail?.let { pd ->
            ProductDetailModal(
                product = pd,
                viewModel = viewModel,
                onDismiss = { activeProductForDetail = null },
                onBuyNow = { onNavigateToTab(1) }
            )
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Column {
                    // Welcome and Headline Block
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(top = 48.dp, bottom = 16.dp, start = 20.dp, end = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                coil.compose.AsyncImage(
                                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                        .data("https://img.icons8.com/color/48/000000/online-store.png")
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "SnowWhite Brand Logo",
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .border(1.dp, MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SnowWhite",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (loggedInUser?.role == "admin" || loggedInUser?.role == "super_admin") {
                                    Box {
                                        IconButton(onClick = { showAdminCategoryMenu = true }) {
                                            Icon(Icons.Default.MoreVert, "Admin Menu")
                                        }
                                        DropdownMenu(
                                            expanded = showAdminCategoryMenu,
                                            onDismissRequest = { showAdminCategoryMenu = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Manage Categories") },
                                                onClick = {
                                                    showAdminCategoryMenu = false
                                                    onNavigateToTab(14)
                                                }
                                            )
                                        }
                                    }
                                }

                                if (loggedInUser?.role != "admin" && loggedInUser?.role != "super_admin") {
                                    IconButton(
                                        onClick = { isWishlistOpen = true },
                                        modifier = Modifier.testTag("nav_wishlist_badge")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = "Wishlist",
                                            tint = Color(0xFFE91E63)
                                        )
                                    }
                                    val qtyCount = cartSummary.items.sumOf { it.cartItem.quantity }
                                    BadgedBox(
                                        badge = {
                                            if (qtyCount > 0) {
                                                Badge(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                                ) {
                                                    Text(text = "$qtyCount")
                                                }
                                            }
                                        }
                                    ) {
                                        IconButton(
                                            onClick = { onNavigateToTab(1) }, // Navigate to Cart
                                            modifier = Modifier.testTag("nav_cart_badge")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ShoppingCart,
                                                contentDescription = "Active Shopping Cart",
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Modern Search Field with cancel controls
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Find pottery, blankets, silvers...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                            leadingIcon = { Icon(Icons.Default.Search, "Search icon", tint = MaterialTheme.colorScheme.primary) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { viewModel.updateSearchQuery("") },
                                        modifier = Modifier.testTag("clear_search_btn")
                                    ) {
                                        Icon(Icons.Default.Clear, "Clear trigger", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                } else {
                                    IconButton(
                                        onClick = { startVoiceSearch() },
                                        modifier = Modifier.testTag("voice_search_btn")
                                    ) {
                                        Icon(Icons.Default.Mic, "Voice search microphone logo", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("catalog_search_bar"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                        )
                    }

                    AutoScrollingCarousel(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    PromoHeader()
                    ServiceCatalog()

                    // Category Selection Stepper/Scrollable chips
                    val categoriesListLocal by viewModel.appCategories.collectAsState()
                    val categories = listOf("All") + categoriesListLocal.map { it.name }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = selectedCat.lowercase() == cat.lowercase()
                            FilterChip(
                                selected = isSelected,
                                onClick = { 
                                    viewModel.selectCategory(cat)
                                    focusManager.clearFocus()
                                },
                                shape = RoundedCornerShape(50),
                                label = { 
                                    Text(
                                        text = when(cat) {
                                            "All" -> "✨ All Services"
                                            "Dry Cleaning" -> "👔 Dry Cleaning"
                                            "Laundry" -> "🧺 Laundry"
                                            "Carpet & Rugs" -> "🧹 Carpet & Rugs"
                                            "Specialized" -> "🧥 Specialized"
                                            "Pickup and Drop" -> "🚚 Pickup and Drop"
                                            else -> cat
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ) 
                                },
                                modifier = Modifier.testTag("category_chip_$cat"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }

                    // Live Promo Banner
                    if (!viewModel.appBannerUrl.isNullOrEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            coil.compose.AsyncImage(
                                model = viewModel.appBannerUrl,
                                contentDescription = "Promotional Banner",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Featured Products", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        TextButton(onClick = { showAllProducts = true }) {
                            Text("View All")
                        }
                    }
                }
            }

            // Main listings viewport
            when {
                apiState is ApiProductState.Loading && products.isEmpty() -> {
                    items(6, span = { androidx.compose.foundation.lazy.grid.GridItemSpan(1) }) {
                        Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                            ShimmerProductCard()
                        }
                    }
                }
                apiState is ApiProductState.Error && products.isEmpty() -> {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = "Connection error",
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Unable to connect to backend",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = (apiState as ApiProductState.Error).message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.loadProductsFromApi() },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Retry Connection")
                                }
                            }
                        }
                    }
                }
                products.isEmpty() -> {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = "Nothing found",
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No goods matched search criteria.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Try clearing queries or changing category tabs.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
                else -> {
                    items(products.take(4), key = { it.id }) { item -> // Show top 4 in the featured list
                        Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                            ProductListingCard(
                                product = item,
                                viewModel = viewModel,
                                onClick = { activeProductForDetail = item },
                                onQuickAdd = { viewModel.addToCart(item) },
                                onBuyNow = { onNavigateToTab(1) }
                            )
                        }
                    }
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            Button(onClick = { showAllProducts = true }) {
                                Text("Explore All Services")
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        FaqAccordion()
                    }
                }
            }
        }

    // Modal popup detail inspection
    activeProductForDetail?.let { pd ->
        ProductDetailModal(
            product = pd,
            viewModel = viewModel,
            onDismiss = { activeProductForDetail = null },
            onBuyNow = { onNavigateToTab(1) }
        )
    }
    
    FloatingActionButton(
        onClick = { showContactSupport = true },
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(Icons.Default.SupportAgent, contentDescription = "Contact Support")
    }

    if (showContactSupport) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showContactSupport = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Contact Support",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Need help? We're here for you 24/7.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Phone", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("1-800-SNOW-WHT", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { 
                        showContactSupport = false
                        onNavigateToTab(5)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(androidx.compose.material.icons.Icons.Default.Chat, contentDescription = "Live Chat")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Live Chat")
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    } // End Box
}

@Composable
fun ProductListingCard(
    product: Product,
    viewModel: MarketViewModel,
    onClick: () -> Unit,
    onQuickAdd: () -> Unit,
    onBuyNow: (() -> Unit)? = null
) {
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val isWishlisted = wishlistIds.contains(product.id)
    val context = androidx.compose.ui.platform.LocalContext.current
    val loggedInUser by viewModel.loggedInUser.collectAsState()
    val isAdmin = loggedInUser?.role == "admin" || loggedInUser?.role == "super_admin"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Box {
                coil.compose.AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                
                // Real-time stock count alerts
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (product.stock == 0) Color(0xFFD32F2F)
                            else if (product.stock <= 2) Color(0xFFFF9800)
                            else Color(0xFF388E3C)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (product.stock == 0) "Sold Out" else "${product.stock} left",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (!isAdmin) {
                    IconButton(
                        onClick = { viewModel.toggleWishlist(product.id) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.7f))
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Toggle Favorite",
                            tint = if (isWishlisted) Color(0xFFE91E63) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = product.artisanName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = product.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (product.subCategory != null) {
                    Text(
                        text = product.subCategory,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rs. ${String.format(Locale.US, "%.2f", product.price)}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (!isAdmin) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = {
                                    onQuickAdd()
                                    android.widget.Toast.makeText(context, "Item added to cart successfully! 🛒", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                enabled = product.stock > 0,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("add_to_cart_btn_${product.id}"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    disabledContainerColor = MaterialTheme.colorScheme.outline
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddShoppingCart,
                                    contentDescription = "Quick add basket",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDetailModal(
    product: Product,
    viewModel: MarketViewModel,
    onDismiss: () -> Unit,
    onBuyNow: (() -> Unit)? = null
) {
    androidx.activity.compose.BackHandler(onBack = onDismiss)
    val context = androidx.compose.ui.platform.LocalContext.current
    val loggedInUser by viewModel.loggedInUser.collectAsState()
    val isAdmin = loggedInUser?.role == "admin" || loggedInUser?.role == "super_admin"
    
    // Changing from Dialog to full-screen page view overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box {
                coil.compose.AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp), // Thicker header for details page
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                // Back button on top left instead of dismiss on top right for a page feel
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .align(Alignment.TopStart)
                ) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                        text = "BY: ${product.artisanName.uppercase()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, "Rating star", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${product.rating}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🏺 Genuine service of ${product.category}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Simple Mock Reviews Section
                    Text(
                        text = "Customer Reviews",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val sampleReviews = listOf(
                            "Great service! They picked up on time.",
                            "The cleaning quality was fantastic. Very satisfied.",
                            "Item arrived perfectly. Thank you!"
                        )
                        sampleReviews.forEachIndexed { idx, reviewText ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("U${idx+1}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("User ${idx+1}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.weight(1f))
                                        Row {
                                            repeat(5) {
                                                Icon(Icons.Default.Star, "Star", tint = if (it < (5 - idx)) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), modifier = Modifier.size(12.dp))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(reviewText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Divider()

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(0.7f)) {
                            Text(
                                text = "PRICE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "Rs. ${String.format(Locale.US, "%.2f", product.price)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (!isAdmin) {
                            Button(
                                onClick = {
                                    viewModel.addToCart(product)
                                    android.widget.Toast.makeText(context, "Added to cart! 🛒", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                enabled = product.stock > 0,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(48.dp).weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text(if (product.stock > 0) "Add to Cart" else "Out of Stock", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
}


// ==========================================
// PART 3: RECTIFIED CART & PAYMENT GATEWAY
// ==========================================

@Composable
fun CartScreen(
    viewModel: MarketViewModel,
    onNavigateToTab: (Int) -> Unit
) {
    val summary by viewModel.cartSummary.collectAsState()
    val isProcessing = viewModel.isPaymentProcessing
    val successOrder = viewModel.paymentResultSuccess
    val errorOrder = viewModel.paymentResultError

    val savedAddresses by viewModel.savedAddresses.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(errorOrder) {
        if (errorOrder == "Please login to place an order") {
            android.widget.Toast.makeText(context, errorOrder, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.acknowledgePaymentResult()
            onNavigateToTab(4)
        }
    }

    // Textfields inputs for credit checkout
    var fullNameStr by remember { mutableStateOf("") }
    var phoneStr by remember { mutableStateOf("") }
    var cardNum by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    var addressStr by remember { mutableStateOf("") }
    var pickupDate by remember { mutableStateOf("") }
    var pickupTime by remember { mutableStateOf("") }
    var deliveryDate by remember { mutableStateOf("") }
    var deliveryTime by remember { mutableStateOf("") }
    var typedPromo by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("cod") }

    // Laundry pickup and delivery schedule selections
    var selectedPickupDate by remember { mutableStateOf("") }
    var selectedPickupSlot by remember { mutableStateOf("") }
    var selectedDeliveryDate by remember { mutableStateOf("") }
    var selectedDeliverySlot by remember { mutableStateOf("") }
    var showSummaryModal by remember { mutableStateOf(false) }

    LaunchedEffect(successOrder) {
        if (successOrder != null) {
            val deliveryText = if (selectedDeliveryDate.isNotEmpty()) selectedDeliveryDate else "3-5 business days"
            android.widget.Toast.makeText(context, "Order Confirmed: ${successOrder.id}. Est. Drop-off: $deliveryText", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    // Read promo states from combined ViewModel
    val couponSuccess by viewModel.couponSuccess.collectAsState()
    val couponError by viewModel.couponError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App top title
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 48.dp, bottom = 12.dp, start = 20.dp, end = 20.dp)
            ) {
                Text(
                    text = "Secure Desk",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = "Checkout Bag",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        if (summary.items.isEmpty() && successOrder == null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Empty bag",
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your bag is currently empty.",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Browse original pieces and artisan items to populate your checkout.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { onNavigateToTab(0) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Explore Products")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                // Cart Items Section
                if (summary.items.isNotEmpty()) {
                        Text(
                            text = "ORDER ITEMS (${summary.items.sumOf { it.cartItem.quantity }})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                    summary.items.forEach { uiItem ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.removeFromCart(uiItem.product.id)
                                    true
                                } else {
                                    false
                                }
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Delete",
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete item",
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            },
                            enableDismissFromStartToEnd = false,
                            modifier = Modifier.testTag("swipe_dismiss_${uiItem.product.id}")
                        ) {
                            CartItemRow(
                                uiItem = uiItem,
                                onIncrement = { viewModel.addToCart(uiItem.product) },
                                onDecrement = { viewModel.decreaseCartQuantity(uiItem.product.id) },
                                onCancel = { viewModel.removeFromCart(uiItem.product.id) }
                            )
                        }
                    }

                    // Promo Code Section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Snowwhite Promotion Codes",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Use SNOW15 (15% off) or GLOW20 (20% off) for premium loyalty reductions.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = typedPromo,
                                    onValueChange = { typedPromo = it },
                                    placeholder = { Text("e.g., WASH10", fontSize = 13.sp) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .testTag("promo_code_input"),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { 
                                        viewModel.applyPromoCode(typedPromo)
                                        typedPromo = ""
                                    },
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("apply_promo_button"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Apply")
                                }
                            }

                            if (couponSuccess != null) {
                                Text(
                                    text = couponSuccess ?: "",
                                    color = Color(0xFF388E3C),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            } else if (couponError != null) {
                                Text(
                                    text = couponError ?: "",
                                    color = Color(0xFFD32F2F),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }

                    // Financial Tallies receipt
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "RECEIPT SUMMARY",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            ReceiptEntry(label = "Items Subtotal", value = summary.subtotal)
                            if (summary.appliedDiscount > 0.0) {
                                ReceiptEntry(
                                    label = "Discount (${summary.promoCode})",
                                    value = -summary.appliedDiscount,
                                    valueColor = Color(0xFF388E3C)
                                )
                            }
                            ReceiptEntry(label = "Service Tax (8%)", value = summary.tax)
                            ReceiptEntry(
                                label = "Pickup & Drop Fee",
                                value = summary.shippingFee,
                                overrideText = if (summary.shippingFee == 0.0) "FREE" else null
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "ORDER ESTIMATED TOTAL",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Rs. ${String.format(Locale.US, "%.2f", summary.total)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Secured Payment Form (Simulated Sandbox integration with live Card validation details)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Secured check",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "SECURE SANDBOX PAYMENT",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    Text(
                                        text = "SSL ENCRYPTED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // ====================================================
                                // INTERACTIVE LAUNDRY CALENDAR SCHEDULER
                                // ====================================================
                                Text(
                                    text = "LAUNDRY SCHEDULING",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Configure date and dynamic time-slot windows for our clean agents.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                // Dates generator (next 7 days starting tomorrow)
                                val calendar = Calendar.getInstance()
                                val sdfDate = SimpleDateFormat("EEE, MMM d", Locale.US)
                                val dateOptions = remember {
                                    val list = mutableListOf<String>()
                                    // Start tomorrow
                                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                                    for (i in 1..7) {
                                        list.add(sdfDate.format(calendar.time))
                                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                                    }
                                    list
                                }

                                val timeSlots = listOf(
                                    "09:00 AM - 12:00 PM (Morning)",
                                    "12:00 PM - 03:00 PM (Afternoon)",
                                    "03:00 PM - 06:00 PM (Evening)",
                                    "06:00 PM - 09:00 PM (Night)"
                                )

                                // Pickup Picker UI
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Schedule, "Pickup", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("1. Free Pickup Appointment Date", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            items(dateOptions) { opt ->
                                                val isSelected = selectedPickupDate == opt
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = { 
                                                        selectedPickupDate = opt
                                                        // Automatically set delivery date to pickup + 2 days as suggestion
                                                        val pIdx = dateOptions.indexOf(opt)
                                                        if (pIdx >= 0 && selectedDeliveryDate.isEmpty()) {
                                                            val sugIdx = (pIdx + 2).coerceAtMost(dateOptions.lastIndex)
                                                            selectedDeliveryDate = dateOptions[sugIdx]
                                                        }
                                                    },
                                                    label = { Text(opt, fontSize = 11.sp) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Select Pickup Hour Slot", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            items(timeSlots) { slot ->
                                                val isSelected = selectedPickupSlot == slot
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = { selectedPickupSlot = slot },
                                                    label = { Text(slot.substringBefore(" ("), fontSize = 10.sp) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                // Drop-off Picker UI
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocalShipping, "Drop-off", tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("2. Clean Drop-off Appointment Date", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            items(dateOptions) { opt ->
                                                val isSelected = selectedDeliveryDate == opt
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = { selectedDeliveryDate = opt },
                                                    label = { Text(opt, fontSize = 11.sp) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = Color(0xFF2E7D32),
                                                        selectedLabelColor = Color.White
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Select Drop-off Hour Slot", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            items(timeSlots) { slot ->
                                                val isSelected = selectedDeliverySlot == slot
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = { selectedDeliverySlot = slot },
                                                    label = { Text(slot.substringBefore(" ("), fontSize = 10.sp) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = Color(0xFF1B5E20),
                                                        selectedLabelColor = Color.White
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                if (selectedPickupDate.isNotEmpty() && selectedPickupSlot.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Event, "Summary", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Selected Order Window:\nPickup: $selectedPickupDate [$selectedPickupSlot]\nDrop-off: ${selectedDeliveryDate.ifEmpty { "Not Chosen" }} [${selectedDeliverySlot.ifEmpty { "Not Chosen" }}]",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(12.dp))

                                Spacer(modifier = Modifier.height(12.dp))

                                // Real Card validation helper indicator
                                 Text(
                                     text = "Select Payment Method",
                                     style = MaterialTheme.typography.titleSmall,
                                     fontWeight = FontWeight.Bold,
                                     color = MaterialTheme.colorScheme.primary,
                                     modifier = Modifier.padding(bottom = 8.dp)
                                 )

                                 Row(
                                     modifier = Modifier
                                         .fillMaxWidth()
                                         .padding(vertical = 4.dp),
                                     horizontalArrangement = Arrangement.spacedBy(12.dp)
                                 ) {
                                     // COD option
                                     Card(
                                         modifier = Modifier
                                             .weight(1f)
                                             .clickable { selectedPaymentMethod = "cod" }
                                             .testTag("pay_method_cod"),
                                         colors = CardDefaults.cardColors(
                                             containerColor = if (selectedPaymentMethod == "cod") {
                                                 MaterialTheme.colorScheme.primaryContainer
                                             } else {
                                                 MaterialTheme.colorScheme.surface
                                             }
                                         ),
                                         border = if (selectedPaymentMethod == "cod") {
                                             BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                         } else {
                                             BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                         }
                                     ) {
                                         Column(
                                             modifier = Modifier
                                                 .fillMaxWidth()
                                                 .padding(12.dp),
                                             horizontalAlignment = Alignment.CenterHorizontally
                                         ) {
                                             Icon(
                                                 imageVector = Icons.Default.AccountBalanceWallet,
                                                 contentDescription = "COD Icon",
                                                 tint = if (selectedPaymentMethod == "cod") {
                                                     MaterialTheme.colorScheme.primary
                                                 } else {
                                                     MaterialTheme.colorScheme.onSurfaceVariant
                                                 }
                                             )
                                             Spacer(modifier = Modifier.height(6.dp))
                                             Text(
                                                 text = "Cash on Drop-off",
                                                 fontWeight = FontWeight.Bold,
                                                 fontSize = 12.sp,
                                                 color = if (selectedPaymentMethod == "cod") {
                                                     MaterialTheme.colorScheme.onPrimaryContainer
                                                 } else {
                                                     MaterialTheme.colorScheme.onSurface
                                                 }
                                             )
                                         }
                                     }

                                     // Online Payment option
                                     Card(
                                         modifier = Modifier
                                             .weight(1f)
                                             .clickable { selectedPaymentMethod = "card" }
                                             .testTag("pay_method_card"),
                                         colors = CardDefaults.cardColors(
                                             containerColor = if (selectedPaymentMethod == "card") {
                                                 MaterialTheme.colorScheme.primaryContainer
                                             } else {
                                                 MaterialTheme.colorScheme.surface
                                             }
                                         ),
                                         border = if (selectedPaymentMethod == "card") {
                                             BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                         } else {
                                             BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                         }
                                     ) {
                                         Column(
                                             modifier = Modifier
                                                 .fillMaxWidth()
                                                 .padding(12.dp),
                                             horizontalAlignment = Alignment.CenterHorizontally
                                         ) {
                                             Icon(
                                                 imageVector = Icons.Default.CreditCard,
                                                 contentDescription = "Card Icon",
                                                 tint = if (selectedPaymentMethod == "card") {
                                                     MaterialTheme.colorScheme.primary
                                                 } else {
                                                     MaterialTheme.colorScheme.onSurfaceVariant
                                                 }
                                             )
                                             Spacer(modifier = Modifier.height(6.dp))
                                             Text(
                                                 text = "Online Card",
                                                 fontWeight = FontWeight.Bold,
                                                 fontSize = 12.sp,
                                                 color = if (selectedPaymentMethod == "card") {
                                                     MaterialTheme.colorScheme.onPrimaryContainer
                                                 } else {
                                                     MaterialTheme.colorScheme.onSurface
                                                 }
                                             )
                                         }
                                     }
                                 }

                                 Spacer(modifier = Modifier.height(14.dp))

                                 if (selectedPaymentMethod == "cod") {
                                     Row(
                                         modifier = Modifier
                                             .fillMaxWidth()
                                             .clip(RoundedCornerShape(8.dp))
                                             .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                             .padding(12.dp),
                                         verticalAlignment = Alignment.CenterVertically
                                     ) {
                                         Icon(
                                             imageVector = Icons.Default.CheckCircle,
                                             contentDescription = "COD Info",
                                             tint = MaterialTheme.colorScheme.primary,
                                             modifier = Modifier.size(20.dp)
                                         )
                                         Spacer(modifier = Modifier.width(8.dp))
                                         Text(
                                             text = "Cash on Drop-off chosen. Pay with Cash, Card, or UPI upon drop-off.",
                                             fontSize = 11.sp,
                                             fontWeight = FontWeight.SemiBold,
                                             color = MaterialTheme.colorScheme.onPrimaryContainer
                                         )
                                     }
                                     Spacer(modifier = Modifier.height(14.dp))
                                 }

                                 val LuhnCheckPassed = if (selectedPaymentMethod == "cod") true else viewModel.validateCardLuhn(cardNum)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (LuhnCheckPassed) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                        )
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (LuhnCheckPassed) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = "Status symbol",
                                        tint = if (LuhnCheckPassed) Color(0xFF388E3C) else Color(0xFFD32F2F),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (LuhnCheckPassed) "Valid simulated card template (Luhn check OK)." 
                                        else "Please enter a simulated card check (min 12 digits, Luhn valid). Tip: Use card ending with 4, e.g. '4242 4242 4242 4242'.",
                                        fontSize = 10.sp,
                                        color = if (LuhnCheckPassed) Color(0xFF2E7D32) else Color(0xFFC62828),
                                        lineHeight = 13.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                if (selectedPaymentMethod == "card") {
                                    // Outlined digits values
                                    OutlinedTextField(
                                        value = cardNum,
                                        onValueChange = { cardNum = it.filter { char -> char.isDigit() || char == ' ' } },
                                        label = { Text("Credit Card Number") },
                                        placeholder = { Text("e.g., 4242 4242 4242 4242") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("card_number_input"),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = cardHolder,
                                        onValueChange = { cardHolder = it },
                                        label = { Text("Cardholder Name") },
                                        placeholder = { Text("e.g., Elena Kovalyov") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("cardholder_name_input"),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = cardExpiry,
                                            onValueChange = { cardExpiry = it },
                                            label = { Text("Expiry (MM/YY)") },
                                            placeholder = { Text("12/28") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("expiry_input"),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        OutlinedTextField(
                                            value = cardCvv,
                                            onValueChange = { cardCvv = it.filter { char -> char.isDigit() }.take(4) },
                                            label = { Text("CVV") },
                                            placeholder = { Text("123") },
                                            visualTransformation = PasswordVisualTransformation(),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier
                                                .weight(0.8f)
                                                .testTag("cvv_input"),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                if (savedAddresses.isNotEmpty()) {
                                    Text("Saved Addresses", style = MaterialTheme.typography.labelMedium)
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        items(savedAddresses) { addr ->
                                            val isSelected = addressStr == addr.fullAddress
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { addressStr = addr.fullAddress },
                                                label = { Text(addr.title) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = fullNameStr,
                                    onValueChange = { fullNameStr = it },
                                    label = { Text("Full Name") },
                                    placeholder = { Text("e.g. John Doe") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("fullname_input"),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = phoneStr,
                                    onValueChange = { phoneStr = it },
                                    label = { Text("Phone Number") },
                                    placeholder = { Text("e.g. +1 555-1234") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("phone_input"),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = addressStr,
                                    onValueChange = { addressStr = it },
                                    label = { Text("Pickup & Drop Address") },
                                    placeholder = { Text("e.g. 248 Pine Wood Cabin Dr, Portland OR") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("address_input"),
                                    shape = RoundedCornerShape(10.dp),
                                    maxLines = 2
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Real-time feedback for checkout errors
                                if (errorOrder != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth()
                                    ) {
                                        Text(
                                            text = errorOrder,
                                            color = Color(0xFFD32F2F),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        
                                        // Retry button for transient errors
                                        if (errorOrder.contains("Server") || errorOrder.contains("HTTP 5") || errorOrder.contains("network", ignoreCase=true) || errorOrder.contains("timeout", ignoreCase=true) || errorOrder.contains("failed", ignoreCase=true) || errorOrder.contains("Exception", ignoreCase=true) || errorOrder.contains("reach", ignoreCase=true)) {
                                            TextButton(
                                                onClick = {
                                                    viewModel.checkout(
                                                        paymentMethod = selectedPaymentMethod,
                                                        shippingAddress = addressStr,
                                                        phone = phoneStr,
                                                        fullName = fullNameStr,
                                                        pickupSchedule = "$pickupDate $pickupTime",
                                                        deliverySchedule = "$deliveryDate $deliveryTime",
                                                        onSuccess = { onNavigateToTab(3) }
                                                    )
                                                }
                                            ) {
                                                Text("Retry", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (addressStr.trim().isEmpty() || phoneStr.trim().isEmpty() || fullNameStr.trim().isEmpty()) {
                                            android.widget.Toast.makeText(context, "Please fill in your pickup and drop-off details", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            showSummaryModal = true
                                        }
                                    },
                                    enabled = !isProcessing && summary.items.isNotEmpty(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("pay_submit_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    if (isProcessing) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Authenticating Payment ...",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp
                                        )
                                    } else {
                                        Icon(Icons.Default.EnhancedEncryption, "Lock badge")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Pay Secured Rs. ${String.format(java.util.Locale.US, "%.2f", summary.total)}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                }
            }
        }
    }

    // Success Order Placement Modal Overlay
    successOrder?.let { order ->
        CheckoutSuccessDialog(order = order) {
            viewModel.acknowledgePaymentResult()
            // Redirect users to Orders tracking tab (Tab 3)
            onNavigateToTab(3)
            // Empty parameters variables
            cardNum = ""
            cardHolder = ""
            cardExpiry = ""
            cardCvv = ""
            addressStr = ""
        }
    }

    if (showSummaryModal) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSummaryModal = false },
            title = { Text("Order Summary") },
            text = {
                Column {
                    Text("Total Amount: Rs. ${String.format(java.util.Locale.US, "%.2f", summary.total)}")
                    Text("Payment: $selectedPaymentMethod")
                    if (selectedPickupDate.isNotEmpty()) {
                        Text("Pickup: $selectedPickupDate - $selectedPickupSlot")
                    }
                    if (selectedDeliveryDate.isNotEmpty()) {
                        Text("Drop-off: $selectedDeliveryDate - $selectedDeliverySlot")
                    } else {
                        Text("Est. Drop-off: 3-5 business days")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSummaryModal = false
                        viewModel.checkout(
                            paymentMethod = selectedPaymentMethod,
                            shippingAddress = addressStr,
                            phone = phoneStr,
                            fullName = fullNameStr,
                            onSuccess = { onNavigateToTab(3) }
                        )
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSummaryModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CartItemRow(
    uiItem: CartUiItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cart_item_row_${uiItem.product.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle canvas drawing preview representation
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                coil.compose.AsyncImage(
                    model = uiItem.product.imageUrl,
                    contentDescription = uiItem.product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiItem.product.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "By ${uiItem.product.artisanName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Rs. ${String.format(Locale.US, "%.2f", uiItem.product.price)}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "• Stock available: ${uiItem.product.stock}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                    )
                }
            }

            // Product quantity modifiers
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onDecrement,
                    modifier = Modifier
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .testTag("decrease_btn_${uiItem.product.id}")
                ) {
                    Icon(Icons.Default.Remove, "Decrease", modifier = Modifier.size(14.dp))
                }

                Text(
                    text = "${uiItem.cartItem.quantity}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                IconButton(
                    onClick = onIncrement,
                    enabled = uiItem.cartItem.quantity < uiItem.product.stock,
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            if (uiItem.cartItem.quantity < uiItem.product.stock) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            CircleShape
                        )
                        .testTag("increase_btn_${uiItem.product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase quantity",
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("remove_btn_${uiItem.product.id}")
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove product completely",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ReceiptEntry(
    label: String,
    value: Double,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    overrideText: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = overrideText ?: if (value < 0) "-Rs. ${String.format(Locale.US, "%.2f", Math.abs(value))}" 
                   else "Rs. ${String.format(Locale.US, "%.2f", value)}",
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = valueColor
        )
    }
}

@Composable
fun CheckoutSuccessDialog(
    order: Order,
    onContinue: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = Color(0xFF388E3C),
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (order.paymentCardLast4.isEmpty()) "Order Successfully Booked!" else "Payment Authenticated!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (order.paymentCardLast4.isEmpty()) "Your order has been booked as Cash on Drop-off." else "Your order of laundry services is safe.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tracking / Order ID", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(order.id, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Items Summary", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(order.itemsSummary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Charged", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text("Rs. ${String.format(Locale.US, "%.2f", order.totalAmount)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Payment Method", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(if (order.paymentCardLast4.isEmpty()) "Cash on Drop-off (COD)" else order.paymentCardLast4, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dismiss_success_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Track Order Shipment")
                }
            }
        }
    }
}


// ==========================================
// PART 4: REAL-TIME INVENTORY MANAGEMENT (Seller Cabins)
// ==========================================

@Composable
fun AdminInventoryScreen(
    viewModel: MarketViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val loggedInUser by viewModel.loggedInUser.collectAsState()
    val products by viewModel.productsState.collectAsState()

    var showAddForm by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var deletingProductCandidate by remember { mutableStateOf<Product?>(null) }

    // Security Gate variables
    var secureEmail by remember { mutableStateOf("") }
    var securePassword by remember { mutableStateOf("") }
    var securityError by remember { mutableStateOf<String?>(null) }
    var isAuthenticating by remember { mutableStateOf(false) }

    if (loggedInUser == null || !loggedInUser!!.isAdmin) {
        // Render beautiful and high-fidelity Secure Administrator Authorization Gateway
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Security lock",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "ADMIN SECURITY GATEWAY",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Sign-in to access the Snowwhite active inventory catalog panel.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = secureEmail,
                        onValueChange = { secureEmail = it; securityError = null },
                        label = { Text("Admin Email") },
                        placeholder = { Text("admin@snowwhite.com") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = securePassword,
                        onValueChange = { securePassword = it; securityError = null },
                        label = { Text("Security Password") },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    if (securityError != null) {
                        Text(
                            text = securityError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (secureEmail.isBlank() || securePassword.isBlank()) {
                                securityError = "Identify yourself by completing both fields."
                                return@Button
                            }
                            isAuthenticating = true
                            viewModel.login(secureEmail, securePassword) { isOk, errorMsg ->
                                isAuthenticating = false
                                if (isOk) {
                                    securityError = null
                                } else {
                                    securityError = errorMsg ?: "Credentials mismatch: Secure Operative Login Rejected."
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !isAuthenticating,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isAuthenticating) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("Unlock Portal Terminal")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Demo Credentials: admin@snowwhite.com / snowwhiteadmin",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    } else {
        // Authenticated Admin Dashboard Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
        // Top header title bar layout
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 48.dp, bottom = 14.dp, start = 20.dp, end = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Snowwhite Portal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Inventory Control",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Button(
                        onClick = { showAddForm = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("admin_add_new_btn")
                    ) {
                        Icon(Icons.Default.Add, "Add service item")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Piece", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live stats widgets
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val totalGoods = products.size
            val lowStockCount = products.count { it.stock in 1..2 }
            val outOfStockCount = products.count { it.stock == 0 }

            AdminStatBadge(
                title = "Total Catalog",
                value = "$totalGoods items",
                colorScheme = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.weight(1f)
            )
            AdminStatBadge(
                title = "Low Stock",
                value = "$lowStockCount pieces",
                colorScheme = if (lowStockCount > 0) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.surfaceVariant,
                textColor = if (lowStockCount > 0) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            AdminStatBadge(
                title = "Sold Out",
                value = "$outOfStockCount items",
                colorScheme = if (outOfStockCount > 0) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant,
                textColor = if (outOfStockCount > 0) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        
        val adminOrders by viewModel.adminAllOrdersState.collectAsState()
        val formattedChartData = remember(adminOrders) {
            val data = mutableMapOf<String, Pair<Int, Double>>()
            val format = java.text.SimpleDateFormat("MM-dd", java.util.Locale.getDefault())
            val now = System.currentTimeMillis()
            val dayMillis = 24L * 60 * 60 * 1000
            for (i in 6 downTo 0) {
                val dateStr = format.format(java.util.Date(now - i * dayMillis))
                data[dateStr] = Pair(0, 0.0)
            }
            
            val fullTimeFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            adminOrders?.forEach { ord ->
                try {
                    val date = fullTimeFormat.parse(ord.createdAt)
                    if (date != null) {
                        val dStr = format.format(date)
                        if (data.containsKey(dStr)) {
                            val current = data[dStr]!!
                            data[dStr] = Pair(current.first + 1, current.second + ord.totalAmount)
                        }
                    }
                } catch (_: Exception) {}
            }
            data.toList()
        }
        
        AdminDashboardChart(data = formattedChartData, modifier = Modifier.padding(horizontal = 16.dp))
        
        Spacer(modifier = Modifier.height(10.dp))

        // Large list of products admin controls
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .testTag("admin_product_list"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                val isDbConfigured by com.example.data.FirestoreService.isConfigured.collectAsState()
                val dbStatusMsg by com.example.data.FirestoreService.firestoreStatusMessage.collectAsState()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("firestore_status_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDbConfigured) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        }
                    ),
                    border = BorderStroke(1.dp, if (isDbConfigured) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isDbConfigured) Color(0xFF4CAF50) else Color(0xFFFF9800))
                            )
                            Text(
                                text = if (isDbConfigured) "Real-Time Cloud Synchronization" else "Local Database Mode",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDbConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Text(
                            text = dbStatusMsg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Firestore Transaction Schema:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        Text(
                            text = "┌─ /products/{id}/stock - Lock-verified using atomic mutator\n" +
                                   "└─ Multi-Device Race Prevention actively enabled",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "ARTISAN STOCK RECORDS (LIVE SYNCHRONIZED)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp, top = 4.dp)
                )
            }

            items(products, key = { it.id }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_item_card_${item.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            coil.compose.AsyncImage(
                                model = item.imageUrl,
                                contentDescription = item.title,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Provider: ${item.artisanName} | ${item.category}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Rs. ${String.format(Locale.US, "%.2f", item.price)}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Rating: ★${item.rating}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }

                            // Current stock tracker
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (item.stock == 0) Color(0xFFFFEBEE)
                                        else if (item.stock <= 2) Color(0xFFFFF3E0)
                                        else Color(0xFFE8F5E9)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "STOCK",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.stock == 0) Color(0xFFC62828)
                                                else if (item.stock <= 2) Color(0xFFE65100)
                                                else Color(0xFF2E7D32)
                                    )
                                    Text(
                                        text = "${item.stock}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = if (item.stock == 0) Color(0xFFC62828)
                                                else if (item.stock <= 2) Color(0xFFE65100)
                                                else Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Stepper Stock Adjustments & Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Adjust Stock: ", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                
                                IconButton(
                                    onClick = { 
                                        if (item.stock > 0) {
                                            viewModel.updateProductDetails(item.copy(stock = item.stock - 1))
                                        }
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                        .testTag("admin_decrease_stock_${item.id}")
                                ) {
                                    Icon(Icons.Default.Remove, "Minus stock", modifier = Modifier.size(16.dp))
                                }

                                Text(
                                    text = "${item.stock}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )

                                IconButton(
                                    onClick = { viewModel.updateProductDetails(item.copy(stock = item.stock + 1)) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                        .testTag("admin_increase_stock_${item.id}")
                                ) {
                                    Icon(Icons.Default.Add, "Plus stock", modifier = Modifier.size(16.dp))
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = { editingProduct = item },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("admin_edit_${item.id}")
                                ) {
                                    Icon(Icons.Default.Edit, "Edit info details", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }

                                IconButton(
                                    onClick = { deletingProductCandidate = item },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("admin_delete_${item.id}")
                                ) {
                                    Icon(Icons.Default.Delete, "Delete listing", tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Forms triggers
    if (showAddForm) {
        AddEditProductDialog(
            product = null,
            viewModel = viewModel,
            onDismiss = { showAddForm = false },
            onConfirm = { title, desc, price, cat, subCat, stock, artisan, imgUrl ->
                viewModel.uploadProduct(
                    title = title,
                    price = price,
                    stockLeft = stock,
                    imageUrl = imgUrl,
                    description = desc,
                    category = cat,
                    subCategory = subCat
                ) { success, errorMsg ->
                    if (success) {
                        android.widget.Toast.makeText(context, "Product Uploaded Successfully via API!", android.widget.Toast.LENGTH_LONG).show()
                    } else {
                        android.widget.Toast.makeText(context, "API Error: ${errorMsg ?: "Could not upload"}", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
                showAddForm = false
            }
        )
    }

    editingProduct?.let { orig ->
        AddEditProductDialog(
            product = orig,
            viewModel = viewModel,
            onDismiss = { editingProduct = null },
            onConfirm = { title, desc, price, cat, subCat, stock, artisan, imgUrl ->
                viewModel.updateProductRemote(
                    id = orig.id,
                    title = title,
                    price = price,
                    stockLeft = stock,
                    imageUrl = imgUrl,
                    description = desc,
                    category = cat,
                    subCategory = subCat
                ) { success, errorMsg ->
                    if (success) {
                        android.widget.Toast.makeText(context, "Product updated details successfully!", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(context, "Update failed: $errorMsg", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
                editingProduct = null
            }
        )
    }

    deletingProductCandidate?.let { prod ->
        AlertDialog(
            onDismissRequest = { deletingProductCandidate = null },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to permanently delete \"${prod.title}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProductRemote(prod.id) { success, errorMsg ->
                            if (success) {
                                android.widget.Toast.makeText(context, "Item deleted successfully!", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                android.widget.Toast.makeText(context, "Delete failed: $errorMsg", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
                        deletingProductCandidate = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingProductCandidate = null }) {
                    Text("Cancel")
                }
            },
            modifier = Modifier.testTag("delete_confirmation_dialog")
        )
    }
}
}

@Composable
fun AdminStatBadge(
    title: String,
    value: String,
    colorScheme: Color,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colorScheme),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor, maxLines = 1)
        }
    }
}

@Composable
fun AddEditProductDialog(
    product: Product?,
    viewModel: MarketViewModel,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, String, String?, Int, String, String) -> Unit
) {
    var title by remember { mutableStateOf(product?.title ?: "") }
    var desc by remember { mutableStateOf(product?.description ?: "") }
    var priceStr by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var stockStr by remember { mutableStateOf(product?.stock?.toString() ?: "") }
    var artisan by remember { mutableStateOf(product?.artisanName ?: "") }
    var imageUrl by remember { mutableStateOf(product?.imageUrl ?: "") }
    var subCategory by remember { mutableStateOf(product?.subCategory ?: "") }
    
    val catList = listOf("Dry Cleaning", "Laundry", "Carpet & Rugs", "Specialized", "Pickup and Drop")
    var selectedCatIndex by remember { 
        mutableStateOf(catList.indexOfFirst { it.lowercase() == (product?.category?.lowercase() ?: "") }.coerceAtLeast(0)) 
    }

    var errorsStr by remember { mutableStateOf<String?>(null) }

    var isUploadingImage by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    val targetWidth = 800
                    val targetHeight = (targetWidth.toDouble() / bitmap.width * bitmap.height).toInt()
                    val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
                    val outputStream = java.io.ByteArrayOutputStream()
                    scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
                    val bytes = outputStream.toByteArray()
                    
                    val base64String = "data:image/jpeg;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                    isUploadingImage = true
                    viewModel.uploadImage(base64String) { url, errorMsg ->
                        isUploadingImage = false
                        if (url != null) {
                            imageUrl = url
                            android.widget.Toast.makeText(context, "Image uploaded successfully!", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(context, "Upload failed: $errorMsg", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Failed to read image: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (product == null) "Add Service Listing" else "Modify Listing Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Service Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_val_title"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = artisan,
                    onValueChange = { artisan = it },
                    label = { Text("Service Provider") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_val_artisan"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description details") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .testTag("admin_val_desc"),
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Service Image URL") },
                    placeholder = { Text("https://example.com/image.jpg") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_val_image_url"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    enabled = !isUploadingImage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_pick_gallery_image"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    if (isUploadingImage) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Uploading image to live server...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Pick Image",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pick image from gallery")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Price (Rs)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_val_price"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = { stockStr = it },
                        label = { Text("Stock Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_val_stock"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Selector Category Text Row
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                    Text("Select Service Category: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        catList.forEachIndexed { idx, catString ->
                            val active = selectedCatIndex == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (active) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { selectedCatIndex = idx }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = catString,
                                    color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (errorsStr != null) {
                    Text(
                        text = errorsStr ?: "",
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val pr = priceStr.toDoubleOrNull()
                            val st = stockStr.toIntOrNull()
                            if (title.isBlank() || artisan.isBlank() || desc.isBlank() || pr == null || st == null || imageUrl.isBlank()) {
                                errorsStr = "Please fill in all details with valid values."
                            } else if (pr <= 0.0 || st < 0) {
                                errorsStr = "Price must be > 0 and Stock must be >= 0."
                            } else {
                                val finalSubCat = if (catList[selectedCatIndex] == "Pickup and Drop") subCategory else null
                                onConfirm(title, desc, pr, catList[selectedCatIndex], finalSubCat, st, artisan, imageUrl)
                            }
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp)
                            .testTag("admin_dialog_confirm_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Pieces")
                    }
                }
            }
        }
    }
}


// ==========================================
// PART 5: CHRONOLOGICAL ORDER RECORDS TRACKING
// ==========================================

@Composable
fun OrdersScreen(
    viewModel: MarketViewModel,
    onNavigateToTab: (Int) -> Unit
) {
    val orders by viewModel.ordersState.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadOrders()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 48.dp, bottom = 12.dp, start = 20.dp, end = 20.dp)
            ) {
                Text(
                    text = "Customer Ledger",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = "Purchase History",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        if (orders == null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        } else if (orders!!.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "No receipts",
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Purchases Recorded.",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Once you order laundry services via checkout, they appear here with live tracking status.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { onNavigateToTab(0) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Start Shopping")
                    }
                }
            }
        } else {
            var trackingQuery by remember { mutableStateOf("") }
            val displayOrders = if (trackingQuery.isBlank()) orders!! else orders!!.filter { it.id.contains(trackingQuery, ignoreCase = true) }
            var selectedOrder by remember { mutableStateOf<com.example.data.Order?>(null) }
            
            selectedOrder?.let {
                com.example.ui.screens.ReceiptDetailDialog(order = it, onDismiss = { selectedOrder = null })
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = trackingQuery,
                        onValueChange = { trackingQuery = it },
                        placeholder = { Text("Filter your local entries") },
                        leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = "Filter") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { onNavigateToTab(6) }, modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)) {
                        Icon(Icons.Default.Search, "Track Server", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .testTag("orders_list"),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(displayOrders, key = { it.id }) { ord ->
                        OrderHistoryCard(order = ord, onClick = { selectedOrder = ord })
                    }
                }
            }
        }
    }
}

@Composable
fun OrderHistoryCard(order: Order, onClick: (() -> Unit)? = null) {
    val formatter = remember { SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()) }
    val dateString = formatter.format(Date(order.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.id}")
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.id,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = dateString,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Rs. ${String.format(Locale.US, "%.2f", order.totalAmount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val (bgColor, textColor) = when (order.status.lowercase()) {
                        "pending" -> Color(0xFFFFF59D) to Color(0xFFF57F17)
                        "picked up" -> Color(0xFFFFF59D) to Color(0xFFF57F17)
                        "processing" -> Color(0xFFBBDEFB) to Color(0xFF1565C0)
                        "in process" -> Color(0xFFBBDEFB) to Color(0xFF1565C0)
                        "shipped" -> Color(0xFFE1BEE7) to Color(0xFF6A1B9A)
                        "out for delivery" -> Color(0xFFE1BEE7) to Color(0xFF6A1B9A)
                        "delivered" -> Color(0xFFC8E6C9) to Color(0xFF2E7D32)
                        "cancelled" -> Color(0xFFFFCDD2) to Color(0xFFC62828)
                        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Box(
                        modifier = Modifier
                            .background(bgColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = order.status.uppercase(),
                            color = textColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Items Ordered: ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = order.itemsSummary,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(10.dp))

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = "Truck cargo",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Pickup & Drop-off Address:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = order.shippingAddress,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (order.pickupSchedule.isNotEmpty() || order.deliverySchedule.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        if (order.pickupSchedule.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Scheduled Pickup: ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = order.pickupSchedule,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                        if (order.deliverySchedule.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF2E7D32))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Scheduled Drop-off: ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = order.deliverySchedule,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Charged to ${order.paymentCardLast4}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Total: Rs. ${String.format(Locale.US, "%.2f", order.totalAmount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { onClick?.invoke() },
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("View Details", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminDashboardChart(
    data: List<Pair<String, Pair<Int, Double>>>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    
    val maxRevenue = data.maxOfOrNull { it.second.second }?.toFloat() ?: 0f
    val maxVolume = data.maxOfOrNull { it.second.first }?.toFloat() ?: 0f
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "7-Day Revenue & Volume Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.tertiary
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).background(primaryColor, androidx.compose.foundation.shape.CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Revenue (Rs)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).background(secondaryColor, androidx.compose.foundation.shape.CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Orders", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                val width = size.width
                val height = size.height
                val usableHeight = height - 30f // Reserve space for text
                
                val barWidth = width / (data.size * 2f)
                val spacing = barWidth
                
                data.forEachIndexed { index, pair ->
                    val dateStr = pair.first
                    val stats = pair.second
                    
                    val revHeight = if (maxRevenue > 0) (stats.second.toFloat() / maxRevenue) * usableHeight else 0f
                    val volHeight = if (maxVolume > 0) (stats.first.toFloat() / maxVolume) * usableHeight else 0f
                    
                    val xPos = index * (barWidth * 2) + (spacing/2)
                    
                    // Draw Revenue Bar
                    drawRect(
                        color = primaryColor,
                        topLeft = androidx.compose.ui.geometry.Offset(x = xPos, y = usableHeight - revHeight),
                        size = androidx.compose.ui.geometry.Size(width = barWidth * 0.8f, height = revHeight)
                    )
                    
                    // Draw Volume Bar
                    drawRect(
                        color = secondaryColor,
                        topLeft = androidx.compose.ui.geometry.Offset(x = xPos + barWidth * 0.8f, y = usableHeight - volHeight),
                        size = androidx.compose.ui.geometry.Size(width = barWidth * 0.8f, height = volHeight)
                    )
                    
                    // Draw Label X-Axis
                    drawContext.canvas.nativeCanvas.drawText(
                        dateStr,
                        xPos + barWidth*0.8f,
                        height,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 24f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PromoHeader() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
            coil.compose.AsyncImage(
                model = "https://images.unsplash.com/photo-1545173168-9f1947eebb7f?q=80&w=2071&auto=format&fit=crop",
                contentDescription = "Promo Background",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.8f),
                                Color.Black.copy(alpha = 0.2f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "✨ Spring Cleaning Sale!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Get 20% off your first dry cleaning order.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { /* Claim offer logic */ },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Claim Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ServiceCatalog() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ServiceCard("Dry Cleaning", "From Rs. 500/item", Icons.Default.Checkroom)
        ServiceCard("Laundry", "From Rs. 250/lb", Icons.Default.LocalLaundryService)
        ServiceCard("Carpet Restoration", "From Rs. 2500/sqft", Icons.Default.CleaningServices)
    }
}

@Composable
fun ServiceCard(title: String, priceText: String, icon: ImageVector) {
    Card(
        modifier = Modifier.width(140.dp).height(120.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = priceText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AdminProductsScreen(viewModel: MarketViewModel) {
    val loggedInUser by viewModel.loggedInUser.collectAsState()
    val products by viewModel.productsState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var deleteCandidate by remember { mutableStateOf<Product?>(null) }
    var editCandidate by remember { mutableStateOf<Product?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    if (loggedInUser == null || !(loggedInUser!!.role == "admin" || loggedInUser!!.role == "super_admin")) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Access Denied. Admins only.", color = MaterialTheme.colorScheme.error)
        }
        return
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add New Service")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products, key = { it.id }) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        coil.compose.AsyncImage(
                            model = product.imageUrl.takeIf { it.isNotBlank() } ?: "https://via.placeholder.com/150",
                            contentDescription = product.title,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = product.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(text = "Price: Rs. ${product.price}", style = MaterialTheme.typography.bodyMedium)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Stock: ${product.stock}", 
                                    style = MaterialTheme.typography.bodySmall, 
                                    color = if (product.stock > 10) MaterialTheme.colorScheme.primary else if (product.stock > 0) Color(0xFFFFA000) else MaterialTheme.colorScheme.error
                                )
                                if (product.stock <= 10) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    androidx.compose.material3.Badge(
                                        containerColor = if (product.stock == 0) MaterialTheme.colorScheme.error else Color(0xFFFFA000),
                                        contentColor = Color.White
                                    ) {
                                        Text(if (product.stock == 0) "Out of Stock" else "Low Stock", modifier = Modifier.padding(2.dp))
                                    }
                                }
                            }
                        }
                        IconButton(onClick = { editCandidate = product }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Service", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { deleteCandidate = product }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Service", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    deleteCandidate?.let { product ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Delete Service") },
            text = { Text("Are you sure you want to delete '${product.title}'?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.manageProductRemote("delete", productId = product.id) { success, msg ->
                        if (success) {
                            android.widget.Toast.makeText(context, "Deleted", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                    deleteCandidate = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddDialog || editCandidate != null) {
        val isEdit = editCandidate != null
        val product = editCandidate
        AddEditProductDialog(
            isEdit = isEdit,
            initialTitle = product?.title ?: "",
            initialPrice = product?.price?.toString() ?: "",
            initialStock = product?.stock?.toString() ?: "",
            initialImageUrl = product?.imageUrl ?: "",
            initialDescription = product?.description ?: "",
            initialCategory = product?.category ?: "",
            onDismiss = {
                showAddDialog = false
                editCandidate = null
            },
            onSave = { title, price, stock, imageUrl, description, category, subCategory ->
                viewModel.manageProductRemote(
                    action = if (isEdit) "edit" else "add",
                    productId = product?.id,
                    title = title,
                    price = price,
                    stock = stock,
                    imageUrl = imageUrl,
                    description = description,
                    category = category,
                    subCategory = subCategory
                ) { success, msg ->
                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                    if (success) {
                        showAddDialog = false
                        editCandidate = null
                    }
                }
            },
            onUploadImage = { uri, callback ->
                Thread {
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()
                        if (bitmap != null) {
                            val targetWidth = 800
                            val targetHeight = (targetWidth.toDouble() / bitmap.width * bitmap.height).toInt()
                            val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
                            val outputStream = java.io.ByteArrayOutputStream()
                            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
                            val bytes = outputStream.toByteArray()
                            
                            val base64 = "data:image/jpeg;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                            viewModel.uploadImage(base64) { url, error ->
                                callback(url)
                                if (error != null) {
                                    System.err.println("Upload failed: $error")
                                }
                            }
                        } else {
                            callback(null)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback(null)
                    }
                }.start()
            }
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductDialog(
    isEdit: Boolean,
    initialTitle: String,
    initialPrice: String,
    initialStock: String,
    initialImageUrl: String,
    initialDescription: String,
    initialCategory: String,
    onDismiss: () -> Unit,
    onSave: (title: String, price: Double, stock: Int, imageUrl: String, description: String, category: String, subCategory: String?) -> Unit,
    onUploadImage: ((android.net.Uri, (String?) -> Unit) -> Unit)? = null
) {
    var title by remember { mutableStateOf(initialTitle) }
    var subCategory by remember { mutableStateOf("") }
    var price by remember { mutableStateOf(initialPrice) }
    var stock by remember { mutableStateOf(initialStock) }
    var imageUrl by remember { mutableStateOf(initialImageUrl) }
    var description by remember { mutableStateOf(initialDescription) }
    var isUploading by remember { mutableStateOf(false) }

    val catList = listOf("Dry Cleaning", "Laundry", "Carpet & Rugs", "Specialized", "Pickup and Drop")
    var selectedCatIndex by remember { 
        mutableStateOf(catList.indexOfFirst { it.lowercase() == initialCategory.lowercase() }.coerceAtLeast(0)) 
    }
    var catExpanded by remember { mutableStateOf(false) }

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null && onUploadImage != null) {
            isUploading = true
            onUploadImage(uri) { newUrl ->
                isUploading = false
                if (newUrl != null) {
                    imageUrl = newUrl
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Service" else "Add New Service") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                
                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = catExpanded,
                    onExpandedChange = { catExpanded = it }
                ) {
                    OutlinedTextField(
                        value = catList[selectedCatIndex],
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = catExpanded,
                        onDismissRequest = { catExpanded = false }
                    ) {
                        catList.forEachIndexed { index, cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCatIndex = index
                                    catExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                
                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading && onUploadImage != null
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Default.Upload, contentDescription = "Upload Image")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload Image")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val p = price.toDoubleOrNull() ?: 0.0
                val s = stock.toIntOrNull() ?: 0
                if (title.isNotBlank()) {
                    val finalSubCat = if (catList[selectedCatIndex] == "Pickup and Drop") subCategory else null
                    onSave(title, p, s, imageUrl, description, catList[selectedCatIndex], finalSubCat)
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickupScheduler(
    selectedDate: String,
    onDateChange: (String) -> Unit,
    selectedSlot: String,
    onSlotChange: (String) -> Unit,
    careInstructions: String,
    onCareInstructionsChange: (String) -> Unit
) {
    val timeSlots = listOf("Morning (08:00 AM - 12:00 PM)", "Afternoon (12:00 PM - 04:00 PM)", "Evening (04:00 PM - 08:00 PM)")
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LAUNDRY PICK-UP SCHEDULE",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = selectedDate,
                onValueChange = onDateChange,
                label = { Text("Pick-up Date (e.g. YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = "Date") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedSlot,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Preferred Time Slot") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    timeSlots.forEach { slot ->
                        DropdownMenuItem(
                            text = { Text(slot) },
                            onClick = {
                                onSlotChange(slot)
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = careInstructions,
                onValueChange = onCareInstructionsChange,
                label = { Text("Special fabric care instructions") },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                maxLines = 3
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllProductsScreen(
    viewModel: MarketViewModel,
    onBack: () -> Unit,
    onProductClick: (Product) -> Unit,
    onNavigateToCart: () -> Unit
) {
    val products by viewModel.productsState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCat by viewModel.selectedCategory.collectAsState()
    val categoriesList by viewModel.appCategories.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Services", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search services...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(Icons.Default.Search, "Search icon", tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.updateSearchQuery("") },
                            modifier = Modifier.testTag("clear_search_btn")
                        ) {
                            Icon(Icons.Default.Clear, "Clear trigger", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("all_products_search_bar"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            // Category Chips
            val categories = listOf("All") + categoriesList.map { it.name }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCat.lowercase() == cat.lowercase()
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCategory(cat) },
                        shape = RoundedCornerShape(50),
                        label = { 
                            Text(
                                text = cat,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ) 
                        },
                        modifier = Modifier.testTag("category_chip_$cat")
                    )
                }
            }

            if (products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No services match your criteria", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(
                        bottom = 24.dp,
                        start = 16.dp, 
                        end = 16.dp, 
                        top = 8.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxSize().weight(1f)
                ) {
                    items(products, key = { it.id }) { item ->
                        ProductListingCard(
                            product = item,
                            viewModel = viewModel,
                            onClick = { onProductClick(item) },
                            onQuickAdd = { viewModel.addToCart(item) },
                            onBuyNow = onNavigateToCart
                        )
                    }
                }
            }
        }
    }
}
