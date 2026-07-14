package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.DocumentReference
import com.example.viewmodel.CartUiItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object FirestoreService {
    private const val TAG = "FirestoreService"

    private val _isConfigured = MutableStateFlow(false)
    val isConfigured: StateFlow<Boolean> = _isConfigured.asStateFlow()

    private val _firestoreStatusMessage = MutableStateFlow("Firebase initializing...")
    val firestoreStatusMessage: StateFlow<String> = _firestoreStatusMessage.asStateFlow()

    private var db: FirebaseFirestore? = null
    private var listenerRegistration: ListenerRegistration? = null

    fun initialize(context: Context, dao: MarketplaceDao) {
        try {
            val apps = FirebaseApp.getApps(context)
            val app = if (apps.isEmpty()) {
                FirebaseApp.initializeApp(context)
            } else {
                apps[0]
            }

            if (app != null) {
                db = FirebaseFirestore.getInstance()
                _isConfigured.value = true
                _firestoreStatusMessage.value = "Firestore Active & Connected to Cloud Database."
                Log.d(TAG, "Firestore successfully initialized.")
                
                // Start real-time sync from Firestore to local Room Cache
                startRealtimeProductSync(dao)
            } else {
                _isConfigured.value = false
                _firestoreStatusMessage.value = "Firestore inactive (FirebaseApp null). Using local Room cache."
            }
        } catch (e: Exception) {
            _isConfigured.value = false
            _firestoreStatusMessage.value = "No configuration loaded. Running in standard Offline-First client database."
            Log.e(TAG, "Firestore initialization skipped: ${e.localizedMessage}")
        }
    }

    /**
     * Seeds initial products into Cloud Firestore if they do not match.
     */
    fun seedInitialProductsInCloud(products: List<Product>) {
        val database = db ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                for (product in products) {
                    val docRef = database.collection("products").document(product.id.toString())
                    val productMap = mapOf(
                        "id" to product.id,
                        "title" to product.title,
                        "description" to product.description,
                        "price" to product.price,
                        "category" to product.category,
                        "stock" to product.stock,
                        "artisanName" to product.artisanName,
                        "imageUrl" to product.imageUrl,
                        "rating" to product.rating
                    )
                    docRef.set(productMap)
                }
                Log.d(TAG, "Successfully seeded ${products.size} products to Firestore!")
            } catch (e: Exception) {
                Log.e(TAG, "Failed seeding products to Cloud: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Set up continuous listener to sync product document fields (specifically real-time stock levels) from Firestore to local Room cache.
     */
    private fun startRealtimeProductSync(dao: MarketplaceDao) {
        val database = db ?: return
        try {
            listenerRegistration?.remove()
            listenerRegistration = database.collection("products")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e(TAG, "Firestore realtime sync error: ${error.localizedMessage}")
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            for (doc in snapshots.documentChanges) {
                                val docData = doc.document
                                val pId = docData.getLong("id")?.toInt() ?: docData.id.toIntOrNull() ?: continue
                                if (doc.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                                    dao.getProductById(pId)?.let { dao.deleteProduct(it) }
                                } else {
                                    val title = docData.getString("title") ?: ""
                                    val desc = docData.getString("description") ?: ""
                                    val price = docData.getDouble("price") ?: 0.0
                                    val category = docData.getString("category") ?: ""
                                    val subCategory = docData.getString("subCategory")
                                    val stock = docData.getLong("stock")?.toInt() ?: 0
                                    val artisan = docData.getString("artisanName") ?: "Admin"
                                    val img = docData.getString("imageUrl") ?: ""
                                    val p = Product(id = pId, title = title, description = desc, price = price, category = category, subCategory = subCategory, stock = stock, artisanName = artisan, imageUrl = img, rating = 5.0)
                                    val existing = dao.getProductById(pId)
                                    if (existing == null) {
                                        dao.insertProduct(p)
                                    } else {
                                        dao.insertProduct(p) // Room @Insert with REPLACE
                                    }
                                }
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed starting real-time products sync: ${e.localizedMessage}")
        }
    }

    /**
     * Updates an individual product's stock directly in Firestore.
     */
    fun updateStockInCloud(productId: Int, newStock: Int) {
        val database = db ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                database.collection("products").document(productId.toString())
                    .update("stock", newStock)
                Log.d(TAG, "Cloud update: Set product ID $productId stock to $newStock in Firestore")
            } catch (e: Exception) {
                Log.e(TAG, "Failed cloud stock update: ${e.localizedMessage}")
            }
        }
    }

    fun addOrUpdateProductInCloud(product: Product, onComplete: (Boolean) -> Unit = {}) {
        val database = db
        if (database == null) {
            onComplete(false)
            return
        }
        val productMap = mapOf(
            "id" to product.id,
            "title" to product.title,
            "description" to product.description,
            "price" to product.price,
            "category" to product.category,
            "stock" to product.stock,
            "artisanName" to product.artisanName,
            "imageUrl" to product.imageUrl,
            "rating" to product.rating
        )
        database.collection("products").document(product.id.toString())
            .set(productMap)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun deleteProductInCloud(productId: Int, onComplete: (Boolean) -> Unit = {}) {
        val database = db
        if (database == null) {
            onComplete(false)
            return
        }
        database.collection("products").document(productId.toString())
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Executes checkOutCart atomic transaction with Cloud Firestore consistency.
     * Decrements inventory within a Firestore multi-document transaction block.
     * Supports suspend function pattern using coroutines.
     */
    suspend fun performFirestoreCheckoutTransaction(
        cartItems: List<CartUiItem>
    ): Double = suspendCoroutine { continuation ->
        val database = db
        if (database == null) {
            continuation.resumeWithException(Exception("Firestore connection isolated. Please supply configuration."))
            return@suspendCoroutine
        }

        database.runTransaction { transaction ->
            val updates = mutableListOf<Pair<DocumentReference, Int>>()
            var total = 0.0

            for (item in cartItems) {
                val docRef = database.collection("products").document(item.product.id.toString())
                val snapshot = transaction.get(docRef)
                
                if (!snapshot.exists()) {
                    throw Exception("Cloud Product Catalog '${item.product.title}' not found in Firestore.")
                }

                val currentStock = snapshot.getLong("stock")?.toInt()
                    ?: throw Exception("Stock metadata missing for '${item.product.title}' in Cloud database.")

                if (currentStock < item.cartItem.quantity) {
                    throw Exception("Cloud Transaction Aborted: Insufficient real-time stock for '${item.product.title}'. Live Stock: $currentStock.")
                }

                val updatedStock = currentStock - item.cartItem.quantity
                updates.add(docRef to updatedStock)
                total += item.product.price * item.cartItem.quantity
            }

            // Commit atomic increments
            for ((ref, stock) in updates) {
                transaction.update(ref, "stock", stock)
            }
            
            total // Return computed sum on transaction success block
        }.addOnSuccessListener { total ->
            continuation.resume(total)
        }.addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
    }
}
