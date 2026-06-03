package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.viewmodel.CartSummary
import com.example.viewmodel.CartUiItem
import com.example.viewmodel.MarketViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.ui.theme.*

// ==========================================
// PART 1: PROCEDURAL CUSTOM DECORATIVE CANVAS
// Representing hand-crafted items beautifully without relying on faulty web image URLs
// ==========================================

@Composable
fun ProceduralCraftImage(category: String, subkey: String, modifier: Modifier = Modifier) {
    val clayCol = Color(0xFFC77A58)
    val sandCol = Color(0xFFDCC19D)
    val darkMoss = Color(0xFF435A4B)
    val indigoCol = Color(0xFF324D63)
    val goldenGold = Color(0xFFD4AF37)
    val silverMetal = Color(0xFFBEC2C5)
    val woodOak = Color(0xFF865D36)
    val woodChestnut = Color(0xFF5D4037)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.radialGradient(
                    colors = when (category.lowercase()) {
                        "ceramics" -> listOf(Color(0xFFFDFBF7), Color(0xFFEFE6DC))
                        "textiles" -> listOf(Color(0xFFE8ECEF), Color(0xFFD2DCE5))
                        "jewelry" -> listOf(Color(0xFFFDFBF4), Color(0xFFF3ECE1))
                        "woodwork" -> listOf(Color(0xFFFAF2ED), Color(0xFFE7D8CF))
                        else -> listOf(Color.White, Color.LightGray)
                    }
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(90.dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val cx = canvasWidth / 2
            val cy = canvasHeight / 2

            when (category.lowercase()) {
                "ceramics" -> {
                    if (subkey.contains("cups")) {
                        // Two cute espresso cups side-by-side
                        // Cup 1
                        drawArc(
                            color = clayCol,
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = true,
                            size = Size(cx, cy),
                            topLeft = Offset(cx * 0.15f, cy * 0.6f)
                        )
                        drawCircle(color = sandCol, radius = cx * 0.18f, center = Offset(cx * 0.5f, cy * 0.7f))
                        // Cup 2
                        drawArc(
                            color = darkMoss,
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = true,
                            size = Size(cx, cy),
                            topLeft = Offset(cx * 0.85f, cy * 0.7f)
                        )
                    } else {
                        // Ceramic pottery water/flower vase
                        // Base round curve
                        drawCircle(color = clayCol, radius = cx * 0.35f, center = Offset(cx, cy * 1.1f))
                        // Speckled look
                        drawCircle(color = sandCol, radius = 2.5f, center = Offset(cx - 10f, cy * 1.0f))
                        drawCircle(color = sandCol, radius = 3f, center = Offset(cx + 12f, cy * 1.2f))
                        drawCircle(color = Color(0xFF3E2723), radius = 2f, center = Offset(cx - 5f, cy * 1.25f))
                        // Vase neck
                        val neckWidth = cx * 0.3f
                        drawRoundRect(
                            color = sandCol,
                            topLeft = Offset(cx - neckWidth / 2, cy * 0.45f),
                            size = Size(neckWidth, cy * 0.5f),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                        // Vase collar opening
                        drawOval(
                            color = clayCol,
                            topLeft = Offset(cx - neckWidth * 0.6f, cy * 0.38f),
                            size = Size(neckWidth * 1.2f, 10.dp.toPx())
                        )
                    }
                }
                "textiles" -> {
                    if (subkey.contains("apron")) {
                        // Cozy crossback linen apron sketch
                        val path = Path().apply {
                            moveTo(cx * 0.6f, cy * 0.4f)
                            lineTo(cx * 1.4f, cy * 0.4f)
                            lineTo(cx * 1.5f, cy * 1.1f)
                            lineTo(cx * 1.4f, cy * 1.6f)
                            lineTo(cx * 0.6f, cy * 1.6f)
                            lineTo(cx * 0.5f, cy * 1.1f)
                            close()
                        }
                        drawPath(path = path, color = darkMoss)
                        // Canvas front pocket lines
                        drawRoundRect(
                            color = sandCol,
                            topLeft = Offset(cx * 0.75f, cy * 1.0f),
                            size = Size(cx * 0.5f, cy * 0.4f)
                        )
                    } else {
                        // Organic geometric-weaved blanket representation
                        drawRoundRect(
                            color = indigoCol,
                            topLeft = Offset(cx * 0.2f, cy * 0.3f),
                            size = Size(cx * 1.6f, cy * 1.3f),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                        // Decorative horizontal weave patterns
                        for (yOffset in listOf(-25f, -10f, 5f, 20f, 35f)) {
                            drawLine(
                                color = sandCol,
                                start = Offset(cx * 0.3f, cy + yOffset),
                                end = Offset(cx * 1.5f, cy + yOffset),
                                strokeWidth = 3f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 8f), 0f)
                            )
                        }
                        // Fringe tassels at sides
                        for (x in stride(cx * 0.25f, cx * 1.55f, 15f)) {
                            drawLine(
                                color = Color.White,
                                start = Offset(x, cy * 0.3f),
                                end = Offset(x, cy * 0.3f - 12f),
                                strokeWidth = 2.5f
                            )
                            drawLine(
                                color = Color.White,
                                start = Offset(x, cy * 1.6f),
                                end = Offset(x, cy * 1.6f + 12f),
                                strokeWidth = 2.5f
                            )
                        }
                    }
                }
                "jewelry" -> {
                    if (subkey.contains("earrings")) {
                        // Pair of hand-threaded beaded dreamer earrings
                        // Left earring ring & beads
                        drawCircle(color = goldenGold, radius = cx * 0.25f, center = Offset(cx * 0.6f, cy), style = Stroke(width = 3f))
                        for (angle in 0..360 step 45) {
                            val rad = Math.toRadians(angle.toDouble())
                            val bx = cx * 0.6f + (cx * 0.25f) * Math.cos(rad).toFloat()
                            val by = cy + (cx * 0.25f) * Math.sin(rad).toFloat()
                            drawCircle(color = clayCol, radius = 4f, center = Offset(bx, by))
                        }
                        // Right earring ring & beads
                        drawCircle(color = goldenGold, radius = cx * 0.25f, center = Offset(cx * 1.4f, cy), style = Stroke(width = 3f))
                        for (angle in 0..360 step 45) {
                            val rad = Math.toRadians(angle.toDouble())
                            val bx = cx * 1.4f + (cx * 0.25f) * Math.cos(rad).toFloat()
                            val by = cy + (cx * 0.25f) * Math.sin(rad).toFloat()
                            drawCircle(color = Color(0xFFD48A54), radius = 4f, center = Offset(bx, by))
                        }
                    } else {
                        // Cast floral silver ring
                        drawCircle(color = silverMetal, radius = cx * 0.35f, center = Offset(cx, cy * 1.1f), style = Stroke(width = 8f))
                        // Floral wildflower crown
                        drawCircle(color = goldenGold, radius = cx * 0.12f, center = Offset(cx, cy * 0.65f))
                        for (i in 0..5) {
                            val rad = Math.toRadians((i * 60).toDouble())
                            val px = cx + (cx * 0.16f) * Math.cos(rad).toFloat()
                            val py = cy * 0.65f + (cx * 0.16f) * Math.sin(rad).toFloat()
                            drawCircle(color = Color.White, radius = 5f, center = Offset(px, py))
                        }
                    }
                }
                "woodwork" -> {
                    if (subkey.contains("box")) {
                        // Aromatic red cedar carved storage chest
                        drawRoundRect(
                            color = woodOak,
                            topLeft = Offset(cx * 0.3f, cy * 0.5f),
                            size = Size(cx * 1.4f, cy * 1.0f),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                        // Inlaid details / latch
                        drawRect(
                            color = woodChestnut,
                            topLeft = Offset(cx * 0.35f, cy * 0.55f),
                            size = Size(cx * 1.3f, 8f)
                        )
                        drawCircle(color = goldenGold, radius = cx * 0.1f, center = Offset(cx, cy * 0.95f))
                    } else {
                        // End-grain walnut cutting board
                        drawRoundRect(
                            color = woodOak,
                            topLeft = Offset(cx * 0.15f, cy * 0.45f),
                            size = Size(cx * 1.7f, cy * 1.0f),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                        // Woodgrain rings
                        drawRoundRect(
                            color = woodChestnut,
                            topLeft = Offset(cx * 0.25f, cy * 0.55f),
                            size = Size(cx * 1.5f, cy * 0.8f),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                            style = Stroke(width = 3f)
                        )
                        // Grip handle hole
                        drawCircle(color = CreamChiffon, radius = 8f, center = Offset(cx * 1.6f, cy * 0.95f))
                    }
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
fun MainCatalogScreen(
    viewModel: MarketViewModel,
    onNavigateToTab: (Int) -> Unit
) {
    val products by viewModel.productsState.collectAsState()
    val selectedCat by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val cartSummary by viewModel.cartSummary.collectAsState()

    var activeProductForDetail by remember { mutableStateOf<Product?>(null) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Welcome and Headline Block
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 48.dp, bottom = 18.dp, start = 20.dp, end = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Authentic Crafts",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "CraftMarket",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    val qtyCount = cartSummary.items.sumOf { it.cartItem.quantity }
                    BadgedBox(
                        badge = {
                            if (qtyCount > 0) {
                                Badge {
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
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Modern Search Field with cancel controls
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Find pottery, blankets, silvers...") },
                    leadingIcon = { Icon(Icons.Default.Search, "Search icon") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Clear, "Clear trigger")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("catalog_search_bar"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            }
        }

        // Category Selection Stepper/Scrollable chips
        val categories = listOf("All", "Ceramics", "Textiles", "Jewelry", "Woodwork")
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
                    label = { 
                        Text(
                            text = when(cat) {
                                "All" -> "✨ All"
                                "Ceramics" -> "🏺 Ceramics"
                                "Textiles" -> "🧵 Textiles"
                                "Jewelry" -> "💍 Jewelry"
                                "Woodwork" -> "🪓 Woodwork"
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
        if (products.isEmpty()) {
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
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("product_grid")
            ) {
                items(products, key = { it.id }) { item ->
                    ProductListingCard(
                        product = item,
                        onClick = { activeProductForDetail = item },
                        onQuickAdd = { viewModel.addToCart(item) }
                    )
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
    onClick: () -> Unit,
    onQuickAdd: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Box {
                ProceduralCraftImage(category = product.category, subkey = product.imageUrl)
                
                // Real-time stock count alerts
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
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
            }

            Column(modifier = Modifier.padding(12.dp)) {
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

                    IconButton(
                        onClick = onQuickAdd,
                        enabled = product.stock > 0,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (product.stock > 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline
                            )
                            .testTag("add_to_cart_btn_${product.id}"),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
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

@Composable
fun ProductDetailModal(
    product: Product,
    viewModel: MarketViewModel,
    onDismiss: () -> Unit
) {
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
                            Text(if (product.stock > 0) "Add to Bag" else "Out of Stock")
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

    // Textfields inputs for credit checkout
    var cardNum by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    var addressStr by remember { mutableStateOf("") }
    var typedPromo by remember { mutableStateOf("") }

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
                        CartItemRow(
                            uiItem = uiItem,
                            onIncrement = { viewModel.addToCart(uiItem.product) },
                            onDecrement = { viewModel.decreaseCartQuantity(uiItem.product.id) },
                            onCancel = { viewModel.removeFromCart(uiItem.product.id) }
                        )
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
                                    text = "Artisan Coupon Codes",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Use HANDMADE10 (10% off) or ARTISAN20 (20% off).",
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

                                Spacer(modifier = Modifier.height(12.dp))

                                // Real Card validation helper indicator
                                val LuhnCheckPassed = viewModel.validateCardLuhn(cardNum)
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
                                            cardNumber = cardNum,
                                            cardHolder = cardHolder,
                                            expiryDate = cardExpiry,
                                            cvv = cardCvv,
                                            shippingAddress = addressStr
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
                                        Text("Authenticating Payment ...")
                                    } else {
                                        Icon(Icons.Default.EnhancedEncryption, "Lock badge")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Pay Secured $${String.format(Locale.US, "%.2f", summary.total)}")
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
                    text = "Payment Authenticated!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Your order of handmade goods is safe.",
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
                            Text("Payment", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(order.paymentCardLast4, fontSize = 11.sp)
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
    val products by viewModel.productsState.collectAsState()

    var showAddForm by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

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
                            text = "Artisan Portal",
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
                                    text = "Artisan: ${item.artisanName} | ${item.category}",
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
                                    onClick = { viewModel.deleteProduct(item) },
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
            onDismiss = { showAddForm = false },
            onConfirm = { title, desc, price, cat, stock, artisan ->
                viewModel.addNewProduct(title, desc, price, cat, stock, artisan)
                showAddForm = false
            }
        )
    }

    editingProduct?.let { orig ->
        AddEditProductDialog(
            product = orig,
            onDismiss = { editingProduct = null },
            onConfirm = { title, desc, price, cat, stock, artisan ->
                viewModel.updateProductDetails(orig.copy(
                    title = title,
                    description = desc,
                    price = price,
                    category = cat,
                    stock = stock,
                    artisanName = artisan
                ))
                editingProduct = null
            }
        )
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
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, String, Int, String) -> Unit
) {
    var title by remember { mutableStateOf(product?.title ?: "") }
    var desc by remember { mutableStateOf(product?.description ?: "") }
    var priceStr by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var stockStr by remember { mutableStateOf(product?.stock?.toString() ?: "") }
    var artisan by remember { mutableStateOf(product?.artisanName ?: "") }
    
    val catList = listOf("Ceramics", "Textiles", "Jewelry", "Woodwork")
    var selectedCatIndex by remember { 
        mutableStateOf(catList.indexOfFirst { it.lowercase() == (product?.category?.lowercase() ?: "") }.coerceAtLeast(0)) 
    }

    var errorsStr by remember { mutableStateOf<String?>(null) }

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
                    text = if (product == null) "Add Artisan Listing" else "Modify Listing Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Product Title") },
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
                    label = { Text("Artisan Maker Name") },
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
                            if (title.isBlank() || artisan.isBlank() || desc.isBlank() || pr == null || st == null) {
                                errorsStr = "Please fill in all details with valid values."
                            } else if (pr <= 0.0 || st < 0) {
                                errorsStr = "Price must be > 0 and Stock must be >= 0."
                            } else {
                                onConfirm(title, desc, pr, catList[selectedCatIndex], st, artisan)
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

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = order.status,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                        text = "Shipping Address:",
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

            Spacer(modifier = Modifier.height(12.dp))

            // Elegant tracking progress bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Handcrafted Processing", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("In Transit", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                    Text("Delivered", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.primary))
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
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
        }
    }
}
