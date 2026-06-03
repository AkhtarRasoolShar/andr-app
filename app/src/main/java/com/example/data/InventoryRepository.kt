package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class InventoryRepository(private val dao: MarketplaceDao) {

    val allProducts: Flow<List<Product>> = dao.getAllProductsFlow()
    val allCartItems: Flow<List<CartItem>> = dao.getCartItemsFlow()
    val allOrders: Flow<List<Order>> = dao.getAllOrdersFlow()

    suspend fun getProductById(productId: Int): Product? {
        return dao.getProductById(productId)
    }

    suspend fun insertProduct(product: Product): Long {
        return dao.insertProduct(product)
    }

    suspend fun updateProduct(product: Product) {
        dao.updateProduct(product)
    }

    suspend fun deleteProduct(product: Product) {
        // Also remove from cart if present
        dao.deleteCartItemByProductId(product.id)
        dao.deleteProduct(product)
    }

    suspend fun addToCart(productId: Int) {
        val product = dao.getProductById(productId) ?: return
        if (product.stock <= 0) return // Out of stock

        val existing = dao.getCartItemByProductId(productId)
        if (existing == null) {
            dao.insertCartItem(CartItem(productId = productId, quantity = 1))
        } else {
            if (existing.quantity < product.stock) {
                dao.updateCartItem(existing.copy(quantity = existing.quantity + 1))
            }
        }
    }

    suspend fun decreaseCartItem(productId: Int) {
        val existing = dao.getCartItemByProductId(productId) ?: return
        if (existing.quantity <= 1) {
            dao.deleteCartItemByProductId(productId)
        } else {
            dao.updateCartItem(existing.copy(quantity = existing.quantity - 1))
        }
    }

    suspend fun removeFromCart(productId: Int) {
        dao.deleteCartItemByProductId(productId)
    }

    suspend fun clearCart() {
        dao.clearCart()
    }

    /**
     * Places an order. Checks and decrements product inventory.
     * Returns the placed Order on success, or throws an Exception with details if items are out of stock.
     */
    suspend fun checkOutCart(
        cardLast4: String,
        shippingAddress: String
    ): Order {
        val cartList = allCartItems.first()
        if (cartList.isEmpty()) {
            throw Exception("Cart is empty")
        }

        // Validate stock for all items first
        val productsToUpdate = mutableListOf<Pair<Product, Int>>()
        val summaryItemsList = mutableListOf<String>()
        var total = 0.0

        for (cartItem in cartList) {
            val product = dao.getProductById(cartItem.productId)
                ?: throw Exception("Product no longer exists")
            
            if (product.stock < cartItem.quantity) {
                throw Exception("Insufficient stock for '${product.title}'. Only ${product.stock} available.")
            }
            
            val updatedStock = product.stock - cartItem.quantity
            productsToUpdate.add(product to updatedStock)
            
            summaryItemsList.add("${product.title} x${cartItem.quantity}")
            total += product.price * cartItem.quantity
        }

        // Apply changes transactionally
        for ((prod, stock) in productsToUpdate) {
            dao.updateProductStock(prod.id, stock)
        }

        val orderId = "ORD-${(1000..9999).random()}"
        val order = Order(
            id = orderId,
            timestamp = System.currentTimeMillis(),
            itemsSummary = summaryItemsList.joinToString(", "),
            totalAmount = total,
            status = "Processing",
            paymentCardLast4 = cardLast4,
            shippingAddress = shippingAddress
        )

        dao.insertOrder(order)
        dao.clearCart()
        return order
    }

    /**
     * Pre-populates the product catalog with handmade items if empty.
     */
    suspend fun ensureSeededData() {
        val currentList = allProducts.first()
        if (currentList.isEmpty()) {
            getSeedProducts().forEach { dao.insertProduct(it) }
        }
    }

    private fun getSeedProducts(): List<Product> {
        return listOf(
            Product(
                title = "Classic Stoneware Vase",
                description = "Hand-thrown sand-textured pottery vase with organic neck opening and high iron speckling. Elegant container for dried wildflowers.",
                price = 49.00,
                category = "Ceramics",
                stock = 5,
                artisanName = "Elena's Pottery Studio",
                imageUrl = "ceramics_vase",
                rating = 4.9
            ),
            Product(
                title = "Indigo Knit Wool Blanket",
                description = "100% organic virgin Merino wool blanket dyed in deep botanical indigo. Features heavy-weight traditional geometric pattern and hand-rolled tassels.",
                price = 110.00,
                category = "Textiles",
                stock = 3,
                artisanName = "Sven's Weaving Loom",
                imageUrl = "textiles_blanket",
                rating = 4.8
            ),
            Product(
                title = "Wildflower Silver Ring",
                description = "Stoned sterling silver band cast with intricate pressed field flower details. Made using recycled earth-friendly nickel-free metals.",
                price = 55.00,
                category = "Jewelry",
                stock = 8,
                artisanName = "Lumina Fine Metalcraft",
                imageUrl = "jewelry_ring",
                rating = 4.7
            ),
            Product(
                title = "Artisan Walnut Cutting Board",
                description = "End-grain serving board crafted from reclaimed black walnut wood. Finished with food-grade beeswax and organic flaxseed oils.",
                price = 75.00,
                category = "Woodwork",
                stock = 4,
                artisanName = "Forest Edge Carpentry",
                imageUrl = "woodwork_board",
                rating = 4.9
            ),
            Product(
                title = "Drip-Glazed Espresso Set",
                description = "Pair of dual-tone hand-thrown short espresso cups. Distinctive volcanic charcoal drip glaze. Fits comforting in the palm.",
                price = 38.00,
                category = "Ceramics",
                stock = 6,
                artisanName = "Elena's Pottery Studio",
                imageUrl = "ceramics_cups",
                rating = 4.6
            ),
            Product(
                title = "Handwoven Linen Apron",
                description = "Crossback rustic linen apron with double front utility pockets and sturdy bar-tack sewing. Ideal for pottery, painting, or baking.",
                price = 42.00,
                category = "Textiles",
                stock = 10,
                artisanName = "Sven's Weaving Loom",
                imageUrl = "textiles_apron",
                rating = 4.5
            ),
            Product(
                title = "Beaded Dreamer Earrings",
                description = "Intricately hand-threaded glass beads inspired by summer sunrise colors. Features hypoallergenic 14k gold-filled hooks.",
                price = 29.00,
                category = "Jewelry",
                stock = 12,
                artisanName = "Lumina Fine Metalcraft",
                imageUrl = "jewelry_earrings",
                rating = 4.8
            ),
            Product(
                title = "Cedar Leaf Carved Chest",
                description = "Medium-sized box carved from aromatic red cedar wood. Ideal for storage, keepsake preservation, or incense accessories.",
                price = 145.00,
                category = "Woodwork",
                stock = 2,
                artisanName = "Forest Edge Carpentry",
                imageUrl = "woodwork_box",
                rating = 5.0
            )
        )
    }
}
