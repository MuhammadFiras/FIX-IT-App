package com.example.fixit.data.remote.service

import com.example.fixit.domain.model.ServiceOrder
import kotlinx.coroutines.flow.Flow

interface RemoteDataSource {
    suspend fun createServiceOrder(order: ServiceOrder): Result<ServiceOrder> // Returns ServiceOrder with ID
    fun getServiceOrders(): Flow<List<ServiceOrder>>
    fun getServiceOrderById(orderId: String): Flow<ServiceOrder>
    suspend fun updateServiceOrder(order: ServiceOrder): Result<Unit>
    suspend fun deleteServiceOrder(orderId: String): Result<Unit>
}
