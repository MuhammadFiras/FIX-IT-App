package com.example.fixit.data.repository.impl

import com.example.fixit.data.local.dao.ServiceOrderDao
import com.example.fixit.data.local.entities.toDomainModel
import com.example.fixit.data.local.entities.toEntity
import com.example.fixit.data.remote.service.FirebaseServiceOrderDataSource
import com.example.fixit.domain.model.ServiceOrder
import com.example.fixit.domain.repository.ServiceOrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.catch
import android.util.Log
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel // Pastikan ini diimpor

class ServiceOrderRepositoryImpl(
    private val remoteDataSource: FirebaseServiceOrderDataSource,
    private val localDataSource: ServiceOrderDao
) : ServiceOrderRepository {

    // Buat CoroutineScope untuk repository
    // SupervisorJob agar kegagalan anak tidak membatalkan yang lain
    private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Fungsi bantu untuk melakukan sinkronisasi penuh dari Firebase ke Room
    private suspend fun syncAllOrdersFromFirebaseToRoom() {
        withContext(Dispatchers.IO) {
            try {
                val remoteOrders = remoteDataSource.getServiceOrders().first() // .first() mengambil snapshot pertama dari Flow Firebase
                localDataSource.deleteAllOrders() // Menghapus semua entitas lama
                localDataSource.insertAllOrders(remoteOrders.map { it.toEntity() }) // Menyisipkan entitas baru
                Log.d("RepositorySync", "Full sync from Firebase to Room: ${remoteOrders.size} orders.")
            } catch (e: Exception) {
                Log.e("RepositorySync", "Failed to sync all orders from Firebase to Room: ${e.message}")
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
                        syncAllOrdersFromFirebaseToRoom() // Panggil sync penuh setelah create
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
                        syncAllOrdersFromFirebaseToRoom() // Panggil sync penuh setelah update
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
                    syncAllOrdersFromFirebaseToRoom() // Panggil sync penuh setelah delete
                }
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
        // HAPUS BLOK .onEach { ... } DI SINI
    }

    override fun getServiceOrderById(orderId: String): Flow<ServiceOrder> {
        return localDataSource.getOrderById(orderId)
            .map { it?.toDomainModel() ?: throw NoSuchElementException("Order not found in local cache") }
            .catch { e ->
                Log.e("Repository", "Error from local database flow in getServiceOrderById: ${e.message}")
                throw e
            }
    }

    // Fungsi getActiveServiceOrders() tanpa .onEach
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
        // HAPUS BLOK .onEach { ... } DI SINI
    }

    override suspend fun insertAllOrdersToLocal(orders: List<ServiceOrder>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                localDataSource.insertAllOrders(orders.map { it.toEntity() })
                Log.d("CacheCheck", "Successfully inserted/updated ${orders.size} orders into local Room DB.")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("CacheCheck", "Failed to insert/update all orders into Room DB: ${e.message}")
                Result.failure(e)
            }
        }
    }

    fun cancelScope() {
        repositoryScope.cancel()
        Log.d("RepositorySync", "Repository scope cancelled.")
    }
}