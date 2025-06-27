package com.example.fixit.domain.usecase

import com.example.fixit.domain.model.ServiceOrder
import com.example.fixit.domain.repository.ServiceOrderRepository
import kotlinx.coroutines.flow.Flow

// Data class untuk mengelompokkan semua use cases terkait ServiceOrder
data class ServiceOrderUseCases(
    val createServiceOrder: CreateServiceOrderUseCase,
    val getServiceOrders: GetServiceOrdersUseCase,
    val getServiceOrderById: GetServiceOrderByIdUseCase,
    val updateServiceOrder: UpdateServiceOrderUseCase,
    val deleteServiceOrder: DeleteServiceOrderUseCase,
    val getActiveServiceOrders: GetActiveServiceOrdersUseCase,
    val insertAllOrdersToLocal: InsertAllOrdersToLocalUseCase,
    val getCompletedServiceOrders: GetCompletedServiceOrdersUseCase,
    val syncAllOrdersFromFirebaseToRoom: SyncAllOrdersFromFirebaseToRoomUseCase, // Pastikan ini ada dan namanya persis
    val getServiceOrdersRealTime: GetServiceOrdersRealTimeUseCase // <-- TAMBAHKAN INI
)

// --- Definisi Use Case Individual ---

class CreateServiceOrderUseCase(private val repository: ServiceOrderRepository) {
    suspend operator fun invoke(order: ServiceOrder): Result<ServiceOrder> {
        if (order.customerName.isBlank() || order.customerPhone.isBlank() || order.serviceDescription.isBlank()) {
            return Result.failure(IllegalArgumentException("Customer name, phone, and service description cannot be empty."))
        }
        return repository.createServiceOrder(order)
    }
}

class GetServiceOrdersUseCase(private val repository: ServiceOrderRepository) {
    operator fun invoke(): Flow<List<ServiceOrder>> {
        return repository.getServiceOrders()
    }
}

class GetServiceOrderByIdUseCase(private val repository: ServiceOrderRepository) {
    operator fun invoke(orderId: String): Flow<ServiceOrder> {
        return repository.getServiceOrderById(orderId)
    }
}

class UpdateServiceOrderUseCase(private val repository: ServiceOrderRepository) {
    suspend operator fun invoke(order: ServiceOrder): Result<Unit> {
        if (order.id.isBlank()) {
            return Result.failure(IllegalArgumentException("Order ID cannot be empty for update."))
        }
        return repository.updateServiceOrder(order)
    }
}

class DeleteServiceOrderUseCase(private val repository: ServiceOrderRepository) {
    suspend operator fun invoke(orderId: String): Result<Unit> {
        if (orderId.isBlank()) {
            return Result.failure(IllegalArgumentException("Order ID cannot be empty for delete."))
        }
        return repository.deleteServiceOrder(orderId)
    }
}

class GetActiveServiceOrdersUseCase(private val repository: ServiceOrderRepository) {
    operator fun invoke(): Flow<List<ServiceOrder>> {
        return repository.getActiveServiceOrders()
    }
}

class InsertAllOrdersToLocalUseCase(private val repository: ServiceOrderRepository) {
    suspend operator fun invoke(orders: List<ServiceOrder>): Result<Unit> {
        return repository.insertAllOrdersToLocal(orders)
    }
}

class GetCompletedServiceOrdersUseCase(private val repository: ServiceOrderRepository) {
    operator fun invoke(): Flow<List<ServiceOrder>> {
        return repository.getCompletedServiceOrders()
    }
}

// DEFINISI KELAS USE CASE UNTUK SINKRONISASI PENUH
class SyncAllOrdersFromFirebaseToRoomUseCase(private val repository: ServiceOrderRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.syncAllOrdersFromFirebaseToRoom()
    }
}

// Use Case baru untuk mendapatkan Flow real-time langsung dari RemoteDataSource
class GetServiceOrdersRealTimeUseCase(private val repository: ServiceOrderRepository) {
    operator fun invoke(): Flow<List<ServiceOrder>> {
        // Repository akan mendelegasikan ke FirebaseServiceOrderDataSource.allServiceOrders
        return repository.getServiceOrdersRealTime() // <-- Akan ditambahkan di Repository
    }
}