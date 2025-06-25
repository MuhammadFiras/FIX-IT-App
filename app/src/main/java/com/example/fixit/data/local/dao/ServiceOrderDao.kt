package com.example.fixit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fixit.data.local.entities.ServiceOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceOrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // REPLACE is crucial for updates
    suspend fun insertOrder(order: ServiceOrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE) // REPLACE is crucial for updates
    suspend fun insertAllOrders(orders: List<ServiceOrderEntity>)

    @Query("SELECT * FROM service_orders WHERE id = :orderId")
    fun getOrderById(orderId: String): Flow<ServiceOrderEntity?>

    @Query("SELECT * FROM service_orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<ServiceOrderEntity>>

    @Query("SELECT * FROM service_orders WHERE status != 'Completed' ORDER BY timestamp DESC")
    fun getActiveOrders(): Flow<List<ServiceOrderEntity>>

    @Query("SELECT * FROM service_orders WHERE status = 'Completed' ORDER BY timestamp DESC")
    fun getCompletedOrders(): Flow<List<ServiceOrderEntity>>

    @Update
    suspend fun updateOrder(order: ServiceOrderEntity)

    @Query("DELETE FROM service_orders WHERE id = :orderId")
    suspend fun deleteOrderById(orderId: String)

    @Query("DELETE FROM service_orders")
    suspend fun deleteAllOrders()
}