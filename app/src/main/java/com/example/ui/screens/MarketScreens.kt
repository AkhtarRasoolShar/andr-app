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

@Composable
fun MainCatalogScreen(
    viewModel: MarketViewModel,
    onNavigateToTab: (Int) -> Unit
) {
    val products by viewModel.productsState.collectAsState()
    val selectedCat by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val cartSummary by viewModel.cartSummary.collectAsState()

    var activeProductForDetail by remember { mutableStateOf<Product?>(null) }
    var isWishlistOpen by remember { mutableStateOf(false) }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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

        // Category Selection Stepper/Scrollable chips
        val categories = listOf("All", "Dry Cleaning", "Laundry", "Carpet & Rugs", "Specialized")
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

        // Main listings viewport
        val apiState by viewModel.apiState.collectAsState()

        when {
            apiState is ApiProductState.Loading && products.isEmpty() -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(6) {
                        ShimmerProductCard()
                    }
                }
            }
            apiState is ApiProductState.Error && products.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
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
            products.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
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
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 24.dp, start = 16.dp, end = 16.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("product_grid")
                ) {
                    items(products, key = { it.id }) { item ->
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
    }

    // Modal popup detail inspection
    activeProductForDetail?.let { pd ->
        ProductDetailModal(
            product = pd,
            viewModel = viewModel,
            onDismiss = { activeProductForDetail = null }
        )
    }
}

@Composable
fun ProductListingCard(
    product: Product,
    viewModel: MarketViewModel,
    onClick: () -> Unit,
    onQuickAdd: () -> Unit
) {
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val isWishlisted = wishlistIds.contains(product.id)
    val context = androidx.compose.ui.platform.LocalContext.current

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
                ProceduralCraftImage(category = product.category, subkey = product.imageUrl)
                
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

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", product.price)}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

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
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
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
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Box {
                    ProceduralCraftImage(
                        category = product.category,
                        subkey = product.imageUrl,
                        modifier = Modifier.height(180.dp)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(Icons.Default.Close, "Dismiss model", tint = Color.White)
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
                                text = "🏺 Genuine product of ${product.category}",
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

                    Spacer(modifier = Modifier.height(20.dp))

                    Divider()

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ESTIMATED TOTAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", product.price)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.addToCart(product)
                                android.widget.Toast.makeText(context, "Item added to cart successfully! 🛒", android.widget.Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            enabled = product.stock > 0,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("modal_buy_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Icon(Icons.Default.ShoppingCartCheckout, "Add checkout symbol")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (product.stock > 0) "Add to Bag" else "Out of Stock",
                                fontWeight = FontWeight.Bold
                            )
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
    var cardNum by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    var addressStr by remember { mutableStateOf("") }
    var typedPromo by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("cod") }

    // Laundry pickup and delivery schedule selections
    var selectedPickupDate by remember { mutableStateOf("") }
    var selectedPickupSlot by remember { mutableStateOf("") }
    var selectedDeliveryDate by remember { mutableStateOf("") }
    var selectedDeliverySlot by remember { mutableStateOf("") }

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
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                // Cart Items Section
                if (summary.items.isNotEmpty()) {
                    item {
                        Text(
                            text = "ORDER ITEMS (${summary.items.sumOf { it.cartItem.quantity }})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(summary.items, key = { it.cartItem.id }) { uiItem ->
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
                    item {
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
                                        placeholder = { Text("e.g., HANDMADE10", fontSize = 13.sp) },
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
                    }

                    // Promo Code Input
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = typedPromo,
                                    onValueChange = { typedPromo = it.uppercase() },
                                    label = { Text("Enter Promo Code") },
                                    modifier = Modifier.weight(1f).height(60.dp),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.applyPromoCode(typedPromo) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(56.dp)
                                ) {
                                    Text("Apply", fontWeight = FontWeight.Bold)
                                }
                            }
                            if (couponSuccess != null) {
                                Text(
                                    text = couponSuccess!!,
                                    color = Color(0xFF388E3C),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 16.dp, bottom = 12.dp, end = 16.dp)
                                )
                            }
                            if (couponError != null) {
                                Text(
                                    text = couponError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 16.dp, bottom = 12.dp, end = 16.dp)
                                )
                            }
                        }
                    }

                    // Financial Tallies receipt
                    item {
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
                                ReceiptEntry(label = "Craftsman Tax (8%)", value = summary.tax)
                                ReceiptEntry(
                                    label = "Secured Shipping",
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
                                        text = "$${String.format(Locale.US, "%.2f", summary.total)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Secured Payment Form (Simulated Sandbox integration with live Card validation details)
                    item {
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

                                // Delivery Picker UI
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocalShipping, "Delivery", tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("2. Clean Delivery Appointment Date", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
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
                                        Text("Select Delivery Hour Slot", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
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
                                            text = "Selected Order Window:\nPickup: $selectedPickupDate [$selectedPickupSlot]\nDelivery: ${selectedDeliveryDate.ifEmpty { "Not Chosen" }} [${selectedDeliverySlot.ifEmpty { "Not Chosen" }}]",
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
                                                 text = "Cash on Delivery",
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
                                             text = "Cash on Delivery chosen. Pay with Cash, Card, or UPI upon delivery.",
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

                                // Outlined digits values
                                OutlinedTextField(
                                    value = cardNum,
                                    onValueChange = { cardNum = it.filter { char -> char.isDigit() || char == ' ' } },
                                    label = { Text("Credit Card Number") },
                                    placeholder = { Text("e.g., 4242 4242 4242 4242") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("card_number_input").then(if (selectedPaymentMethod == "cod") Modifier.size(0.dp) else Modifier),
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
                                        .testTag("cardholder_name_input").then(if (selectedPaymentMethod == "cod") Modifier.size(0.dp) else Modifier),
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
                                            .testTag("expiry_input").then(if (selectedPaymentMethod == "cod") Modifier.size(0.dp) else Modifier),
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
                                            .testTag("cvv_input").then(if (selectedPaymentMethod == "cod") Modifier.size(0.dp) else Modifier),
                                        shape = RoundedCornerShape(10.dp)
                                    )
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
                                    value = addressStr,
                                    onValueChange = { addressStr = it },
                                    label = { Text("Shipping Address") },
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
                                    Text(
                                        text = errorOrder,
                                        color = Color(0xFFD32F2F),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                }

                                Button(
                                    onClick = {
                                        viewModel.checkout(
                                            paymentMethod = selectedPaymentMethod,
                                            cardNumber = cardNum,
                                            cardHolder = cardHolder,
                                            expiryDate = cardExpiry,
                                            cvv = cardCvv,
                                            shippingAddress = addressStr,
                                            pickupSchedule = if (selectedPickupDate.isNotEmpty() && selectedPickupSlot.isNotEmpty()) "$selectedPickupDate | $selectedPickupSlot" else "",
                                            deliverySchedule = if (selectedDeliveryDate.isNotEmpty() && selectedDeliverySlot.isNotEmpty()) "$selectedDeliveryDate | $selectedDeliverySlot" else ""
                                        )
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
                                            text = "Pay Secured $${String.format(java.util.Locale.US, "%.2f", summary.total)}",
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
                ProceduralCraftImage(
                    category = uiItem.product.category,
                    subkey = uiItem.product.imageUrl,
                    modifier = Modifier.size(56.dp)
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
                        text = "$${String.format(Locale.US, "%.2f", uiItem.product.price)}",
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
            text = overrideText ?: if (value < 0) "-$${String.format(Locale.US, "%.2f", Math.abs(value))}" 
                   else "$${String.format(Locale.US, "%.2f", value)}",
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
                    text = if (order.paymentCardLast4.isEmpty()) "Your order has been booked as Cash on Delivery." else "Your order of handmade goods is safe.",
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
                            Text("Transaction ID", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(order.id, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Items Summary", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(order.itemsSummary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Charged", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text("$${String.format(Locale.US, "%.2f", order.totalAmount)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Payment Method", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(if (order.paymentCardLast4.isEmpty()) "Cash on Delivery (COD)" else order.paymentCardLast4, fontSize = 11.sp)
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
                        Icon(Icons.Default.Add, "Add craft item")
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
                            ProceduralCraftImage(
                                category = item.category,
                                subkey = item.imageUrl,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp))
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
                                        text = "$${String.format(Locale.US, "%.2f", item.price)}",
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
            onConfirm = { title, desc, price, cat, stock, artisan, imgUrl ->
                viewModel.uploadProduct(
                    title = title,
                    price = price,
                    stockLeft = stock,
                    imageUrl = imgUrl,
                    description = desc,
                    category = cat
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
            onConfirm = { title, desc, price, cat, stock, artisan, imgUrl ->
                viewModel.updateProductRemote(
                    id = orig.id,
                    title = title,
                    price = price,
                    stockLeft = stock,
                    imageUrl = imgUrl,
                    description = desc,
                    category = cat
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
    onConfirm: (String, String, Double, String, Int, String, String) -> Unit
) {
    var title by remember { mutableStateOf(product?.title ?: "") }
    var desc by remember { mutableStateOf(product?.description ?: "") }
    var priceStr by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var stockStr by remember { mutableStateOf(product?.stock?.toString() ?: "") }
    var artisan by remember { mutableStateOf(product?.artisanName ?: "") }
    var imageUrl by remember { mutableStateOf(product?.imageUrl ?: "") }
    
    val catList = listOf("Dry Cleaning", "Laundry", "Carpet & Rugs", "Specialized")
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
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                if (bytes != null) {
                    val base64String = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
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
                    label = { Text("Product Image URL") },
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
                        label = { Text("Price ($)") },
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
                    Text("Select Craft Category: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
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
                                onConfirm(title, desc, pr, catList[selectedCatIndex], st, artisan, imageUrl)
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

        if (orders.isEmpty()) {
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
                        text = "Once you buy handcrafted creations via checkout, they appear here with live tracking status.",
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
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .testTag("orders_list"),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(orders, key = { it.id }) { ord ->
                    OrderHistoryCard(order = ord)
                }
            }
        }
    }
}

@Composable
fun OrderHistoryCard(order: Order) {
    val formatter = remember { SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()) }
    val dateString = formatter.format(Date(order.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.id}"),
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

                Text(
                    text = "$${String.format(Locale.US, "%.2f", order.totalAmount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Visual Status Timeline
            val steps = listOf("Placed", "Processing", "Shipped", "Delivered")
            val rawStatus = order.status
            val isCancelled = rawStatus.equals("Cancelled", ignoreCase = true)
            val currentStatus = if (rawStatus == "Pending") "Placed" else rawStatus // map to our steps
            val currentIndex = if (isCancelled) -1 else (steps.indexOf(currentStatus).takeIf { it >= 0 } ?: 0)

            val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                    animation = androidx.compose.animation.core.tween(1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (isCancelled) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD32F2F)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cancelled", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = "Cancelled",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                } else {
                    steps.forEachIndexed { index, step ->
                        val isCompleted = index < currentIndex
                        val isActive = index == currentIndex
                        val isFuture = index > currentIndex

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                // Left line segment
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(2.dp)
                                        .background(
                                            if (index == 0) Color.Transparent
                                            else if (index <= currentIndex) MaterialTheme.colorScheme.primary
                                            else Color(0xFFE0E0E0)
                                        )
                                )
                                // Circle
                                Box(
                                    modifier = Modifier
                                        .size(if (isActive) 28.dp else 24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isCompleted) MaterialTheme.colorScheme.primary
                                            else if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha)
                                            else Color(0xFFE0E0E0)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompleted) {
                                        Icon(Icons.Default.Check, contentDescription = "Done", tint = Color.White, modifier = Modifier.size(16.dp))
                                    } else if (isActive) {
                                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color.White))
                                    }
                                }
                                // Right line segment
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(2.dp)
                                        .background(
                                            if (index == steps.size - 1) Color.Transparent
                                            else if (isCompleted) MaterialTheme.colorScheme.primary
                                            else Color(0xFFE0E0E0)
                                        )
                                )
                            }
                            
                            Text(
                                text = step,
                                fontSize = 11.sp,
                                fontWeight = if (isActive || isCompleted) FontWeight.Bold else FontWeight.Normal,
                                color = if (isActive) MaterialTheme.colorScheme.primary else if (isCompleted) MaterialTheme.colorScheme.onSurface else Color.Gray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
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
                        text = "Pickup & Delivery Address:",
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
                                    text = "Scheduled Delivery: ",
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
                    text = "Total: $${String.format(Locale.US, "%.2f", order.totalAmount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { /* No-Op for now, just visual */ },
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
