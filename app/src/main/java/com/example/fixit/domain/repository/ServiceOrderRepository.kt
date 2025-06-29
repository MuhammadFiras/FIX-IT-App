package com.example.fixit.domain.repository

import com.example.fixit.domain.model.ServiceOrder
import kotlinx.coroutines.flow.Flow

interface ServiceOrderRepository {
    suspend fun createServiceOrder(order: ServiceOrder): Result<ServiceOrder>
    fun getServiceOrders(): Flow<List<ServiceOrder>>
    fun getServiceOrderById(orderId: String): Flow<ServiceOrder>
    suspend fun updateServiceOrder(order: ServiceOrder): Result<Unit>
    suspend fun deleteServiceOrder(orderId: String): Result<Unit>
    fun getActiveServiceOrders(): Flow<List<ServiceOrder>>
    suspend fun insertAllOrdersToLocal(orders: List<ServiceOrder>): Result<Unit>
    fun getCompletedServiceOrders(): Flow<List<ServiceOrder>>
    suspend fun syncAllOrdersFromFirebaseToRoom(): Result<Unit>
    fun getServiceOrdersRealTime(): Flow<List<ServiceOrder>>
}