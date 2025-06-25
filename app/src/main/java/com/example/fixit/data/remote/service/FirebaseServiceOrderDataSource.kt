package com.example.fixit.data.remote.service

import com.example.fixit.domain.model.ServiceOrder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch // Pastikan ini diimpor
import kotlinx.coroutines.tasks.await // Pastikan ini diimpor
import android.util.Log // Pastikan ini diimpor
// import com.google.firebase.firestore.DocumentChange // Mungkin perlu ini jika ingin proses per jenis perubahan

const val SERVICE_ORDERS_COLLECTION = "service_orders"

class FirebaseServiceOrderDataSource(
    private val firestore: FirebaseFirestore
) : RemoteDataSource {

    private val serviceOrdersCollection = firestore.collection(SERVICE_ORDERS_COLLECTION)

    override suspend fun createServiceOrder(order: ServiceOrder): Result<ServiceOrder> {
        return try {
            val documentRef = serviceOrdersCollection.document(order.id.ifEmpty { serviceOrdersCollection.document().id })
            val orderToSave = order.copy(id = documentRef.id)
            documentRef.set(orderToSave).await()
            Log.d("FirebaseDataSource", "Order created/updated in Firestore: ${documentRef.id}")
            Result.success(orderToSave)
        } catch (e: Exception) {
            Log.e("FirebaseDataSource", "Error creating/updating order in Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    override fun getServiceOrders(): Flow<List<ServiceOrder>> = callbackFlow {
        Log.d("FirebaseDataSource", "getServiceOrders: callbackFlow started. Adding snapshot listener.")
        val listenerRegistration = serviceOrdersCollection
            .addSnapshotListener { snapshot, e ->
                Log.d("FirebaseDataSource", "addSnapshotListener triggered for all orders.")
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
                    Log.d("FirebaseDataSource", "Firestore Snapshot: ${snapshot.documents.size} raw docs, ${serviceOrders.size} converted orders. Attempting to send to Flow.")

                    // Kembali ke 'send()' yang suspending, dibungkus launch
                    // Ini lebih menjamin bahwa setiap emisi akan dikirim,
                    // menunggu jika channel penuh
                    launch { // <-- KEMBALI KE LAUNCH{} SEND()
                        try {
                            send(serviceOrders)
                            Log.d("FirebaseDataSource", "Flow sent ${serviceOrders.size} orders successfully.")
                        } catch (channelException: Exception) {
                            Log.e("FirebaseDataSource", "Flow send failed: ${channelException.message}")
                            // Jika channel dibatalkan (misal awaitClose aktif), ini akan terjadi
                            if (channelException is kotlinx.coroutines.channels.ClosedSendChannelException) {
                                Log.w("FirebaseDataSource", "Channel closed, cannot send orders.")
                            }
                        }
                    }
                }
            }
        awaitClose {
            Log.d("FirebaseDataSource", "Closing Firestore snapshot listener for all orders. AwaitClose triggered.")
            listenerRegistration.remove()
        }
    }

    override fun getServiceOrderById(orderId: String): Flow<ServiceOrder> = callbackFlow {
        Log.d("FirebaseDataSource", "getServiceOrderById: callbackFlow started. Adding snapshot listener for ID: $orderId.")
        val listenerRegistration = serviceOrdersCollection.document(orderId)
            .addSnapshotListener { snapshot, e ->
                Log.d("FirebaseDataSource", "addSnapshotListener triggered for ID: $orderId.")
                if (e != null) {
                    Log.e("FirebaseDataSource", "Error listening for single order: ${e.message}")
                    close(e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    snapshot.toObject<ServiceOrder>()?.let { order ->
                        Log.d("FirebaseDataSource", "Fetched single order ${order.id} from Firestore. Attempting to send to Flow.")
                        launch { // <-- KEMBALI KE LAUNCH{} SEND()
                            try {
                                send(order)
                                Log.d("FirebaseDataSource", "Successfully sent single order via Flow.")
                            } catch (channelException: Exception) {
                                Log.e("FirebaseDataSource", "Flow send failed: ${channelException.message}")
                                if (channelException is kotlinx.coroutines.channels.ClosedSendChannelException) {
                                    Log.w("FirebaseDataSource", "Channel closed, cannot send order.")
                                }
                            }
                        }
                    }
                } else {
                    Log.w("FirebaseDataSource", "Order with ID $orderId not found or deleted.")
                    close(NoSuchElementException("Service order with ID $orderId not found."))
                }
            }
        awaitClose {
            Log.d("FirebaseDataSource", "Closing Firestore snapshot listener for single order $orderId. AwaitClose triggered.")
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