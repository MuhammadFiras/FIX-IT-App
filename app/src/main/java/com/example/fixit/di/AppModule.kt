// app/src/main/java/com.example.fixit.di/AppModule.kt
package com.example.fixit.di

import android.content.Context
import com.example.fixit.data.local.dao.ServiceOrderDao
import com.example.fixit.data.local.database.AppDatabase
import com.example.fixit.data.remote.service.FirebaseServiceOrderDataSource
import com.example.fixit.data.remote.service.RemoteDataSource // Ini interface remote, butuh @Binds kalau pakai ini
import com.example.fixit.data.repository.impl.ServiceOrderRepositoryImpl
import com.example.fixit.domain.repository.ServiceOrderRepository
import com.example.fixit.domain.usecase.*
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton
import androidx.room.Room
import dagger.Binds // <-- TAMBAHKAN INI

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- PROVIDE CONTEXT APLIKASI ---
    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }

    // --- PROVIDE INSTANCE FIREBASE FIRESTORE ---
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    // --- PROVIDE ROOM DATABASE ---
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "fixit_database"
        ).build()
    }

    // --- PROVIDE ROOM DAO ---
    @Provides
    @Singleton
    fun provideServiceOrderDao(database: AppDatabase): ServiceOrderDao {
        return database.serviceOrderDao()
    }

    // --- PROVIDE SERVICE ORDER USE CASES ---
    // Karena semua use case individu sudah di @Inject constructor,
    // Hilt akan secara otomatis membangun ServiceOrderUseCases ini.
    @Provides
    @Singleton
    fun provideServiceOrderUseCases(
        repository: ServiceOrderRepository // Hilt akan menyediakan ini
    ): ServiceOrderUseCases {
        return ServiceOrderUseCases(
            createServiceOrder = CreateServiceOrderUseCase(repository),
            getServiceOrders = GetServiceOrdersUseCase(repository),
            getServiceOrderById = GetServiceOrderByIdUseCase(repository),
            updateServiceOrder = UpdateServiceOrderUseCase(repository),
            deleteServiceOrder = DeleteServiceOrderUseCase(repository),
            getActiveServiceOrders = GetActiveServiceOrdersUseCase(repository),
            insertAllOrdersToLocal = InsertAllOrdersToLocalUseCase(repository),
            getCompletedServiceOrders = GetCompletedServiceOrdersUseCase(repository),
            syncAllOrdersFromFirebaseToRoom = SyncAllOrdersFromFirebaseToRoomUseCase(repository),
            getServiceOrdersRealTime = GetServiceOrdersRealTimeUseCase(repository)
        )
    }
}

// Module terpisah untuk @Binds (khusus interface ke implementasi)
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule { // <-- Gunakan 'abstract class' untuk @Binds

    // --- BIND REPOSITORY INTERFACE KE IMPLEMENTASI ---
    // Ini memberitahu Hilt: "Jika ada yang meminta ServiceOrderRepository (interface),
    // berikan instance ServiceOrderRepositoryImpl."
    @Binds // <-- Gunakan @Binds
    @Singleton
    abstract fun bindServiceOrderRepository(
        serviceOrderRepositoryImpl: ServiceOrderRepositoryImpl // Hilt akan membangun ini
    ): ServiceOrderRepository

    // --- BIND REMOTE DATA SOURCE INTERFACE KE IMPLEMENTASI (Jika Anda memiliki interface RemoteDataSource) ---
    // @Binds
    // @Singleton
    // abstract fun bindRemoteDataSource(
    //    firebaseServiceOrderDataSource: FirebaseServiceOrderDataSource
    // ): RemoteDataSource
}