package com.example.fixit.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ServiceOrder(
    val id: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val serviceCategory: String = "",
    val serviceDescription: String = "",
    val locationText: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val status: String = "Pending",
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable