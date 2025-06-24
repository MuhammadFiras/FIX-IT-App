package com.example.fixit.data.repository.impl

import com.example.fixit.data.local.dao.ServiceOrderDao
import com.example.fixit.data.local.entities.toDomainModel
import com.example.fixit.data.local.entities.toEntity
import com.example.fixit.data.remote.service.FirebaseServiceOrderDataSource
import com.example.fixit.domain.model.ServiceOrder
import com.example.fixit.domain.repository.ServiceOrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.catch
import android.util.Log
import kotlinx.coroutines.launch
// Hapus import kotlinx.coroutines.CoroutineScope
// Hapus import kotlinx.coroutines.flow.onCompletion

class ServiceOrderRepositoryImpl(
    private val remoteDataSource: FirebaseServiceOrderDataSource,
    private val localDataSource: ServiceOrderDao
) : ServiceOrderRepository {

    // Di ServiceOrderRepositoryImpl.kt
    override suspend fun createServiceOrder(order: ServiceOrder): Result<ServiceOrder> { // <-- PASTIKAN INI Result<ServiceOrder>
        return withContext(Dispatchers.IO) {
            val result = remoteDataSource.createServiceOrder(order)
            result.onSuccess { createdOrder -> // <-- TERIMA createdOrder DI SINI
                launch {
                    try {
                        // Pastikan yang disisipkan ke Room adalah createdOrder, BUKAN 'order' asli
                        localDataSource.insertOrder(createdOrder.toEntity())
                        Log.d("CacheCheck", "Order ${createdOrder.id} inserted into local Room DB after Firebase success.")
                    } catch (e: Exception) {
                        Log.e("CacheCheck", "Failed to insert order into Room DB after Firebase success: ${e.message}")
                    }
                }
            }
            return@withContext result // Kembalikan result asli yang berisi createdOrder
        }
    }

    // Juga di getServiceOrders() dan getActiveServiceOrders()
    override fun getServiceOrders(): Flow<List<ServiceOrder>> {
        return localDataSource.getAllOrders()
            .map { entities ->
                val orders = entities.map { it.toDomainModel() }
                Log.d("Repository", "Fetched ${orders.size} orders from Room (getAllOrders).")
                orders
            }
            .catch { e ->
                Log.e("Repository", "Error from local DB flow in getServiceOrders: ${e.message}")
                emit(emptyList())
            }
    }

    override fun getServiceOrderById(orderId: String): Flow<ServiceOrder> {

        return localDataSource.getOrderById(orderId)
            .map { it?.toDomainModel() ?: throw NoSuchElementException("Order not found in local cache") }
            .catch { e ->
                Log.e("CacheCheck", "Error from local database flow in getServiceOrderById: ${e.message}")
                throw e
            }
    }

    override suspend fun updateServiceOrder(order: ServiceOrder): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val result = remoteDataSource.updateServiceOrder(order)
            result.onSuccess {
                localDataSource.updateOrder(order.toEntity())
                Log.d("CacheCheck", "Order ${order.id} updated in local Room DB.")
            }
            result
        }
    }

    // Di data/repository/impl/ServiceOrderRepositoryImpl.kt
    override suspend fun deleteServiceOrder(orderId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val result = remoteDataSource.deleteServiceOrder(orderId)
            result.onSuccess {
                localDataSource.deleteOrderById(orderId)
                Log.d("CacheCheck", "Order $orderId deleted from local Room DB.") // Log tambahan
            }
            result
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
                Log.e("Repository", "Error from local DB flow in getActiveServiceOrders: ${e.message}")
                emit(emptyList())
            }
    }

    // Di ServiceOrderRepositoryImpl.kt
    override suspend fun insertAllOrdersToLocal(orders: List<ServiceOrder>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                localDataSource.insertAllOrders(orders.map { it.toEntity() })
                Log.d("Repository", "Inserted/Updated ${orders.size} orders into Room DB.")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("Repository", "Failed to insert/update all orders into Room DB: ${e.message}")
                Result.failure(e)
            }
        }
    }
}