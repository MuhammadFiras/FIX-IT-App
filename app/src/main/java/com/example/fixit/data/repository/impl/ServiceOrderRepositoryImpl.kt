package com.example.fixit.data.repository.impl

import com.example.fixit.data.local.dao.ServiceOrderDao
import com.example.fixit.data.local.entities.toDomainModel
import com.example.fixit.data.local.entities.toEntity
import com.example.fixit.data.remote.service.FirebaseServiceOrderDataSource
import com.example.fixit.domain.model.ServiceOrder
import com.example.fixit.domain.repository.ServiceOrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first // Pastikan ini diimpor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.catch
import android.util.Log
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.onEach // HAPUS import ini jika tidak ada .onEach lagi yang pakai
import javax.inject.Inject

class ServiceOrderRepositoryImpl @Inject constructor(
    private val remoteDataSource: FirebaseServiceOrderDataSource,
    private val localDataSource: ServiceOrderDao
) : ServiceOrderRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override suspend fun syncAllOrdersFromFirebaseToRoom(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("RepositorySync", "Starting full sync process from Firebase to Room.")
                val remoteOrdersFlow = remoteDataSource.getServiceOrders()

                val remoteOrders = remoteOrdersFlow.first()

                if (remoteOrders.isNotEmpty()) {
                    localDataSource.deleteAllOrders()
                    Log.d("RepositorySync", "Cleared all existing orders from Room DB.")
                    localDataSource.insertAllOrders(remoteOrders.map { it.toEntity() })
                    Log.d("RepositorySync", "Successfully inserted/updated ${remoteOrders.size} orders into Room DB.")
                } else {
                    Log.d("RepositorySync", "Remote orders fetched empty. Skipping deleteAll and insertAll to preserve local cache if offline.")
                }
                Log.d("RepositorySync", "Full sync from Firebase to Room finished.")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("RepositorySync", "Failed to sync all orders from Firebase to Room: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    override suspend fun createServiceOrder(order: ServiceOrder): Result<ServiceOrder> {
        return withContext(Dispatchers.IO) {
            val result = remoteDataSource.createServiceOrder(order)
            result.onSuccess { createdOrder ->
                repositoryScope.launch {
                    try {
                        localDataSource.insertOrder(createdOrder.toEntity())
                        Log.d("CacheCheck", "Order ${createdOrder.id} inserted into local Room DB after Firebase success.")
                        // syncAllOrdersFromFirebaseToRoom() // <-- PANGGIL KEMBALI INI (tetap panggil ini)
                    } catch (e: Exception) {
                        Log.e("CacheCheck", "Failed to insert order into Room DB after Firebase success: ${e.message}")
                    }
                }
            }
            return@withContext result
        }
    }

    override suspend fun updateServiceOrder(order: ServiceOrder): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val result = remoteDataSource.updateServiceOrder(order)
            result.onSuccess {
                repositoryScope.launch {
                    try {
                        localDataSource.updateOrder(order.toEntity())
                        Log.d("Repository", "Order ${order.id} updated in local Room DB successfully to status ${order.status}.")
                        syncAllOrdersFromFirebaseToRoom() // <-- PANGGIL KEMBALI INI (tetap panggil ini)
                    } catch (e: Exception) {
                        Log.e("Repository", "Failed to update order ${order.id} in local Room DB: ${e.message}.")
                    }
                }
            }
            result
        }
    }

    override suspend fun deleteServiceOrder(orderId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            Log.d("Repository", "Attempting to delete order $orderId from Firebase and Room.")
            val result = remoteDataSource.deleteServiceOrder(orderId)
            result.onSuccess {
                Log.d("Repository", "Successfully deleted order $orderId from Firebase. Now deleting from Room.")
                repositoryScope.launch {
                    try {
                        localDataSource.deleteOrderById(orderId) // Hapus dari Room
                        Log.d("CacheCheck", "Order $orderId deleted from local Room DB.")
                        localDataSource.deleteAllOrders() // Hapus semua yang lama di Room (tambahan Anda)
                        syncAllOrdersFromFirebaseToRoom() // <-- PANGGIL KEMBALI INI (tetap panggil ini)
                    } catch (e: Exception) {
                        Log.e("CacheCheck", "Failed to delete order $orderId from local Room DB: ${e.message}.")
                    }
                }
            }.onFailure { e ->
                Log.e("Repository", "Failed to delete order $orderId from Firebase: ${e.message}.")
            }
            result
        }
    }

    // Fungsi getServiceOrders() tanpa .onEach
    override fun getServiceOrders(): Flow<List<ServiceOrder>> {
        return localDataSource.getAllOrders()
            .map { entities ->
                val orders = entities.map { it.toDomainModel() }
                Log.d("Repository", "Fetched ${orders.size} orders from Room (getAllOrders).")
                orders
            }
            .catch { e ->
                Log.e("Repository", "Error from local database flow in getServiceOrders: ${e.message}")
                emit(emptyList())
            }
    }

    override fun getServiceOrderById(orderId: String): Flow<ServiceOrder> {
        return localDataSource.getOrderById(orderId)
            .map { it?.toDomainModel() ?: throw NoSuchElementException("Order not found in local cache") }
            .catch { e ->
                Log.e("Repository", "Error from local database flow in getServiceOrderById: ${e.message}")
                throw e
            }
    }

    override fun getActiveServiceOrders(): Flow<List<ServiceOrder>> {
        return localDataSource.getActiveOrders()
            .map { entities ->
                val orders = entities.map { it.toDomainModel() }
                Log.d("Repository", "Fetched ${orders.size} active orders from Room (getActiveOrders).")
                orders
            }
            .catch { e ->
                Log.e("Repository", "Error from local database flow in getActiveOrders: ${e.message}")
                emit(emptyList())
            }
    }

    override suspend fun insertAllOrdersToLocal(orders: List<ServiceOrder>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                localDataSource.insertAllOrders(orders.map { it.toEntity() })
                Log.d("CacheCheck", "Successfully inserted/updated ${orders.size} orders into local Room DB (from insertAllOrdersToLocal).")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("CacheCheck", "Failed to insert/update all orders into Room DB (from insertAllOrdersToLocal): ${e.message}")
                Result.failure(e)
            }
        }
    }

    fun cancelScope() {
        repositoryScope.cancel()
        Log.d("RepositorySync", "Repository scope cancelled.")
    }

    override fun getCompletedServiceOrders(): Flow<List<ServiceOrder>> {
        return localDataSource.getCompletedOrders()
            .map { entities -> entities.map { it.toDomainModel() } }
            .catch { e ->
                Log.e("Repository", "Error from local DB flow in getCompletedServiceOrders: ${e.message}")
                emit(emptyList())
            }
    }

    override fun getServiceOrdersRealTime(): Flow<List<ServiceOrder>> {
        Log.d("Repository", "getServiceOrdersRealTime called. Delegating to FirebaseDataSource.allServiceOrders.")
        return remoteDataSource.allServiceOrders
    }
}