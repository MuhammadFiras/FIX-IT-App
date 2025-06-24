package com.example.fixit.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.fixit.data.local.dao.ServiceOrderDao
import com.example.fixit.data.local.entities.ServiceOrderEntity

@Database(entities = [ServiceOrderEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceOrderDao(): ServiceOrderDao
}