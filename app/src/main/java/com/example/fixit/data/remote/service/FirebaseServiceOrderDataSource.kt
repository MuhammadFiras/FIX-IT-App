package com.example.fixit.data.remote.service

import com.example.fixit.domain.model.ServiceOrder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log
import com.google.firebase.firestore.DocumentChange // <-- Pastikan ini diimpor
import kotlinx.coroutines.flow.MutableSharedFlow // Pastikan ini diimpor jika pakai SharedFlow
import kotlinx.coroutines.flow.SharedFlow // Pastikan ini diimpor jika pakai SharedFlow
import kotlinx.coroutines.flow.asSharedFlow // Pastikan ini diimpor jika pakai SharedFlow
import kotlinx.coroutines.CoroutineScope // Pastikan ini diimpor jika pakai SharedFlow
import kotlinx.coroutines.SupervisorJob // Pastikan ini diimpor jika pakai SharedFlow
import kotlinx.coroutines.Dispatchers // Pastikan ini diimpor jika pakai SharedFlow
import kotlinx.coroutines.cancel // Pastikan ini diimpor jika pakai SharedFlow
import kotlinx.coroutines.channels.BufferOverflow // Pastikan ini diimpor jika pakai SharedFlow
import kotlinx.coroutines.flow.conflate
import javax.inject.Inject

const val SERVICE_ORDERS_COLLECTION = "service_orders"

private val firestoreListenerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

class FirebaseServiceOrderDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : RemoteDataSource {

    private val serviceOrdersCollection = firestore.collection(SERVICE_ORDERS_COLLECTION)

    // SharedFlow yang akan memancarkan daftar order terbaru
    private val _allServiceOrders = MutableSharedFlow<List<ServiceOrder>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val allServiceOrders: SharedFlow<List<ServiceOrder>> = _allServiceOrders.asSharedFlow()

    init {
        Log.d("FirebaseDataSource", "FirebaseServiceOrderDataSource init. Attaching snapshot listener.")
        firestoreListenerScope.launch {
            val listenerRegistration = serviceOrdersCollection
                .addSnapshotListener { snapshot, e ->
                    Log.d("FirebaseDataSource", "addSnapshotListener triggered (from init block).")
                    if (e != null) {
                        Log.e("FirebaseDataSource", "Error listening for orders (from init block listener): ${e.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val currentOrders = snapshot.documents.mapNotNull { document ->
                            document.toObject<ServiceOrder>()
                        }

                        for (change in snapshot.documentChanges) {
                            Log.d("FirebaseDataSource", "DocumentChange Detected: Type=${change.type}, DocId=${change.document.id}, NewStatus=${change.document.getString("status")}")
                        }

                        Log.d("FirebaseDataSource", "Firestore Snapshot received (from init block): ${snapshot.documents.size} raw docs, ${currentOrders.size} converted orders. Attempting to emit to SharedFlow.")
                        val result = _allServiceOrders.tryEmit(currentOrders)
                        Log.d("FirebaseDataSource", "SharedFlow emit result: $result (true = success, false = failed).")
                    }
                }
        }
    }

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

    override fun getServiceOrders(): Flow<List<ServiceOrder>> {
        Log.d("FirebaseDataSource", "getServiceOrders called. Returning SharedFlow.")
        return allServiceOrders
    }

    override fun getServiceOrderById(orderId: String): Flow<ServiceOrder> = callbackFlow {
        Log.d("FirebaseDataSource", "getServiceOrderById: callbackFlow started. Adding snapshot listener for ID: $orderId.")
        val listenerRegistration = serviceOrdersCollection.document(orderId)
            .addSnapshotListener { snapshot, e ->
                Log.d("FirebaseDataSource", "addSnapshotListener triggered for ID: $orderId (inside callback).")
                if (e != null) {
                    Log.e("FirebaseDataSource", "Error listening for single order (inside callback): ${e.message}")
                    close(e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    if (snapshot.exists()) {
                        Log.d("FirebaseDataSource", "Single Doc Snapshot: Id=${snapshot.id}, Status=${snapshot.getString("status")}")
                    } else {
                        Log.d("FirebaseDataSource", "Single Doc Snapshot: Id=${snapshot.id}, Document DOES NOT EXIST.")
                    }
                    snapshot.toObject<ServiceOrder>()?.let { order ->
                        Log.d("FirebaseDataSource", "Fetched single order ${order.id} from Firestore. Attempting to send to Flow.")
                        launch {
                            try {
                                send(order)
                                Log.d("FirebaseDataSource", "Successfully sent single order via Flow.")
                            } catch (channelException: Exception) {
                                Log.e("FirebaseDataSource", "Flow send failed (inside launch): ${channelException.message}")
                                if (channelException is kotlinx.coroutines.channels.ClosedSendChannelException) {
                                    Log.w("FirebaseDataSource", "Channel closed, cannot send order.")
                                }
                            }
                        }
                    }
                }
            }
        awaitClose { /* ... */ listenerRegistration.remove() }
    }.conflate()

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

    fun cancelListenerScope() {
        firestoreListenerScope.cancel()
        Log.d("FirebaseDataSource", "Firestore listener scope cancelled from DataSource.")
    }
}