package com.example.fixit.data.remote.service

import com.example.fixit.domain.model.ServiceOrder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch // Pastikan ini diimpor
import android.util.Log // Pastikan ini diimpor
import kotlinx.coroutines.tasks.await // <-- TAMBAHKAN INI

const val SERVICE_ORDERS_COLLECTION = "service_orders"

class FirebaseServiceOrderDataSource(
    private val firestore: FirebaseFirestore
) : RemoteDataSource { // Pastikan ini mengimplementasikan RemoteDataSource

    private val serviceOrdersCollection = firestore.collection(SERVICE_ORDERS_COLLECTION)

    // Di FirebaseServiceOrderDataSource.kt
    override suspend fun createServiceOrder(order: ServiceOrder): Result<ServiceOrder> { // <-- PASTIKAN INI Result<ServiceOrder>
        return try {
            val documentRef = serviceOrdersCollection.document(order.id.ifEmpty { serviceOrdersCollection.document().id })
            val orderToSave = order.copy(id = documentRef.id) // <-- PASTIKAN ID DITETAPKAN DI SINI
            documentRef.set(orderToSave).await() // <-- PASTIKAN await() ada dan diimport
            Log.d("FirebaseDataSource", "Order created/updated in Firestore: ${documentRef.id}")
            Result.success(orderToSave) // <-- PASTIKAN MENGEMBALIKAN orderToSave (dengan ID)
        } catch (e: Exception) {
            Log.e("FirebaseDataSource", "Error creating/updating order in Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    override fun getServiceOrders(): Flow<List<ServiceOrder>> = callbackFlow {
        val listenerRegistration = serviceOrdersCollection
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FirebaseDataSource", "Error listening for orders: ${e.message}")
                    close(e) // Tutup flow jika ada error
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val serviceOrders = snapshot.documents.mapNotNull { document ->
                        val order = document.toObject<ServiceOrder>()
                        if (order == null) {
                            Log.w("FirebaseDataSource", "Failed to convert document ${document.id} to ServiceOrder (null conversion). Check data types/fields in Firestore matching ServiceOrder.kt.")
                        }
                        order
                    }
                    // Log ini yang paling penting
                    Log.d("FirebaseDataSource", "Firestore Snapshot: ${snapshot.documents.size} raw docs, ${serviceOrders.size} converted orders. Attempting to send to Flow.")
                    // Menggunakan trySend().getOrThrow() untuk melihat jika ada error pengiriman
                    val sendResult = trySend(serviceOrders)
                    if (sendResult.isSuccess) {
                        Log.d("FirebaseDataSource", "Flow sent ${serviceOrders.size} orders successfully.")
                    } else if (sendResult.isClosed) {
                        Log.w("FirebaseDataSource", "Flow channel is closed, could not send orders.")
                    } else if (sendResult.isFailure) {
                        Log.e("FirebaseDataSource", "Flow send failed: ${sendResult.exceptionOrNull()?.message}")
                    }
                }
            }
        // Pastikan listener dihapus saat flow tidak lagi diamati
        awaitClose {
            Log.d("FirebaseDataSource", "Closing Firestore snapshot listener for all orders.")
            listenerRegistration.remove()
        }
    }

    override fun getServiceOrderById(orderId: String): Flow<ServiceOrder> = callbackFlow {
        val listenerRegistration = serviceOrdersCollection.document(orderId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FirebaseDataSource", "Error listening for single order: ${e.message}")
                    close(e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    snapshot.toObject<ServiceOrder>()?.let { order ->
                        Log.d("FirebaseDataSource", "Fetched single order ${order.id} from Firestore. Attempting to send to Flow.")
                        val sendResult = trySend(order)
                        if (sendResult.isSuccess) {
                            Log.d("FirebaseDataSource", "Successfully sent single order via Flow.")
                        } else {
                            Log.e("FirebaseDataSource", "Failed to send single order via Flow: ${sendResult.exceptionOrNull()?.message}")
                        }
                    }
                } else {
                    Log.w("FirebaseDataSource", "Order with ID $orderId not found or deleted.")
                    close(NoSuchElementException("Service order with ID $orderId not found."))
                }
            }
        awaitClose {
            Log.d("FirebaseDataSource", "Closing Firestore snapshot listener for single order $orderId.")
            listenerRegistration.remove()
        }
    }

    override suspend fun updateServiceOrder(order: ServiceOrder): Result<Unit> {
        return try {
            serviceOrdersCollection.document(order.id).set(order).await()
            Log.d("FirebaseDataSource", "Order updated in Firestore: ${order.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseDataSource", "Error updating order in Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun deleteServiceOrder(orderId: String): Result<Unit> {
        return try {
            serviceOrdersCollection.document(orderId).delete().await()
            Log.d("FirebaseDataSource", "Order deleted from Firestore: $orderId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseDataSource", "Error deleting order from Firestore: ${e.message}")
            Result.failure(e)
        }
    }
}