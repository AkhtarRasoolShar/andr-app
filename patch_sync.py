import re

with open('app/src/main/java/com/example/data/FirestoreService.kt', 'r') as f:
    content = f.read()

new_sync = """    private fun startRealtimeProductSync(dao: MarketplaceDao) {
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
                                    dao.deleteProduct(pId)
                                } else {
                                    val title = docData.getString("title") ?: ""
                                    val desc = docData.getString("description") ?: ""
                                    val price = docData.getDouble("price") ?: 0.0
                                    val category = docData.getString("category") ?: ""
                                    val stock = docData.getLong("stock")?.toInt() ?: 0
                                    val artisan = docData.getString("artisanName") ?: "Admin"
                                    val img = docData.getString("imageUrl") ?: ""
                                    val p = Product(id = pId, title = title, description = desc, price = price, category = category, stock = stock, artisanName = artisan, imageUrl = img, rating = 5.0)
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
    }"""

content = re.sub(r'    private fun startRealtimeProductSync.*?    \}\n    \}\n', new_sync + '\n', content, flags=re.DOTALL)

with open('app/src/main/java/com/example/data/FirestoreService.kt', 'w') as f:
    f.write(content)
