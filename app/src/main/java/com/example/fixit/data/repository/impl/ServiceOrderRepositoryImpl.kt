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
import kotlinx.coroutines.flow.onEach // <-- Pastikan ini diimpor

class ServiceOrderRepositoryImpl(
    private val remoteDataSource: FirebaseServiceOrderDataSource,
    private val localDataSource: ServiceOrderDao
) : ServiceOrderRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Fungsi bantu untuk melakukan sinkronisasi penuh dari Firebase ke Room
    // Ini akan dipanggil setelah setiap operasi tulis (create, update, delete)
    override suspend fun syncAllOrdersFromFirebaseToRoom(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val remoteOrders = remoteDataSource.getServiceOrders().first()
                localDataSource.insertAllOrders(remoteOrders.map { it.toEntity() })
                Log.d("RepositorySync", "Full sync from Firebase to Room: ${remoteOrders.size} orders.")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("RepositorySync", "Failed to sync all orders from Firebase to Room: ${e.message}")
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
                        localDataSource.deleteAllOrders() // Hapus semua yang lama di Room
                        Log.d("CacheCheck", "Order ${createdOrder.id} inserted into local Room DB after Firebase success.")
                        syncAllOrdersFromFirebaseToRoom() // <-- PANGGIL KEMBALI INI
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
                        syncAllOrdersFromFirebaseToRoom() // <-- PANGGIL KEMBALI INI
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
            val result = remoteDataSource.deleteServiceOrder(orderId)
            result.onSuccess {
                repositoryScope.launch {
                    localDataSource.deleteOrderById(orderId)
                    Log.d("CacheCheck", "Order $orderId deleted from local Room DB.")
                    localDataSource.deleteAllOrders() // Hapus semua yang lama di Room
                    syncAllOrdersFromFirebaseToRoom() // <-- PANGGIL KEMBALI INI
                }
            }
            result
        }
    }

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
            .onEach { // <-- KEMBALIKAN BLOK ON_EACH INI
                this@ServiceOrderRepositoryImpl.repositoryScope.launch {
                    Log.d("RepositorySync", "Triggering full sync from Firebase to Room from getServiceOrders.onEach.")
                    syncAllOrdersFromFirebaseToRoom()
                }
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
            .onEach { // <-- KEMBALIKAN BLOK ON_EACH INI
                this@ServiceOrderRepositoryImpl.repositoryScope.launch {
                    Log.d("RepositorySync", "Triggering full sync from Firebase to Room from getActiveOrders.onEach.")
                    syncAllOrdersFromFirebaseToRoom()
                }
            }
    }

    override suspend fun insertAllOrdersToLocal(orders: List<ServiceOrder>): Result<Unit> {
        // Ini tidak akan dipanggil lagi secara langsung dari ViewModel untuk sync penuh.
        // Hanya updateLocalCache yang akan memanggil insertAllOrders.
        return withContext(Dispatchers.IO) {
            try {
                localDataSource.deleteAllOrders() // Hapus semua yang lama di Room
                localDataSource.insertAllOrders(orders.map { it.toEntity() })
                Log.d("CacheCheck", "Successfully inserted/updated ${orders.size} orders into local Room DB (from insertAllOrdersToLocal).")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("CacheCheck", "Failed to insert/update all orders into Room DB (from insertAllOrdersToLocal): ${e.message}")
                Result.failure(e)
            }
        }
    }

    // --- HAPUS FUNGSI updateLocalCache INI KARENA TIDAK DIGUNAKAN LAGI DALAM STRATEGI INI ---
    // override suspend fun updateLocalCache(latestOrders: List<ServiceOrder>): Result<Unit> { ... }

    fun cancelScope() {
        repositoryScope.cancel()
        Log.d("RepositorySync", "Repository scope cancelled.")
    }

    // Di data/repository/impl/ServiceOrderRepositoryImpl.kt
    override fun getCompletedServiceOrders(): Flow<List<ServiceOrder>> {
        return localDataSource.getCompletedOrders() // Memanggil DAO Room
            .map { entities -> entities.map { it.toDomainModel() } }
            .catch { e ->
                Log.e("Repository", "Error from local DB flow in getCompletedServiceOrders: ${e.message}")
                emit(emptyList())
            }
    }
}