package com.example.fixit.domain.repository

import com.example.fixit.domain.model.ServiceOrder
import kotlinx.coroutines.flow.Flow

interface ServiceOrderRepository {
    suspend fun createServiceOrder(order: ServiceOrder): Result<ServiceOrder> // <-- PASTIKAN INI Result<ServiceOrder>
    fun getServiceOrders(): Flow<List<ServiceOrder>> // Untuk mendapatkan semua order (reactive)
    fun getServiceOrderById(orderId: String): Flow<ServiceOrder> // Untuk mendapatkan order spesifik
    suspend fun updateServiceOrder(order: ServiceOrder): Result<Unit>
    suspend fun deleteServiceOrder(orderId: String): Result<Unit>
    fun getActiveServiceOrders(): Flow<List<ServiceOrder>> // Fungsi baru
    // TAMBAHKAN KEYWORD 'suspend' DI SINI
    suspend fun insertAllOrdersToLocal(orders: List<ServiceOrder>): Result<Unit> // <-- SOLUSI
    // TODO: Tambahkan fungsi lain sesuai kebutuhan BREAD
}