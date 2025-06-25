package com.example.fixit.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fixit.domain.model.ServiceOrder

@Entity(tableName = "service_orders")
data class ServiceOrderEntity(
    @PrimaryKey
    val id: String, // Ensure ID is non-null and correctly used as PK
    val customerName: String,
    val customerPhone: String,
    val serviceCategory: String,
    val serviceDescription: String,
    val locationText: String,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    val timestamp: Long
)

// Mapper functions
fun ServiceOrderEntity.toDomainModel(): ServiceOrder {
    return ServiceOrder(
        id = this.id,
        customerName = this.customerName,
        customerPhone = this.customerPhone,
        serviceCategory = this.serviceCategory,
        serviceDescription = this.serviceDescription,
        locationText = this.locationText,
        latitude = this.latitude,
        longitude = this.longitude,
        status = this.status,
        timestamp = this.timestamp
    )
}

fun ServiceOrder.toEntity(): ServiceOrderEntity {
    return ServiceOrderEntity(
        id = this.id,
        customerName = this.customerName,
        customerPhone = this.customerPhone,
        serviceCategory = this.serviceCategory,
        serviceDescription = this.serviceDescription,
        locationText = this.locationText,
        latitude = this.latitude,
        longitude = this.longitude,
        status = this.status,
        timestamp = this.timestamp
    )
}