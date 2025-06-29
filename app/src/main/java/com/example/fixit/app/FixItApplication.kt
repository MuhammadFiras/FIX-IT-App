package com.example.fixit.app

import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.example.fixit.data.remote.service.FirebaseServiceOrderDataSource
import com.example.fixit.data.repository.impl.ServiceOrderRepositoryImpl
import com.example.fixit.domain.repository.ServiceOrderRepository
import com.example.fixit.domain.usecase.*
import androidx.room.Room
import com.example.fixit.data.local.database.AppDatabase
import com.example.fixit.data.local.dao.ServiceOrderDao
import com.google.android.libraries.places.api.Places
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.firestore.PersistentCacheSettings
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.fixit.util.Constants
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.example.fixit.worker.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import javax.inject.Inject

@HiltAndroidApp
class FixItApplication : Application(), Configuration.Provider { // <-- IMPLEMENTASIKAN Configuration.Provider

    // --- INJEKSI HiltWorkerFactory ---
    @Inject // Hilt akan menyediakan ini
    lateinit var workerFactory: HiltWorkerFactory // <-- TAMBAHKAN INI

    // --- HAPUS SEMUA VARIABEL LATEINIT UNTUK DEPENDENSI MANUAL ---
    // lateinit var database: AppDatabase
    // lateinit var serviceOrderDao: ServiceOrderDao
    // lateinit var firebaseServiceOrderDataSource: FirebaseServiceOrderDataSource
    // lateinit var serviceOrderRepository: ServiceOrderRepository
    // lateinit var serviceOrderUseCases: ServiceOrderUseCases
    // lateinit var serviceOrderRepositoryImpl: ServiceOrderRepositoryImpl

    // --- HAPUS COMPANION OBJECT INSTANCE ---
    // companion object {
    //     lateinit var instance: FixItApplication
    //         private set
    // }


    override fun onCreate() {
        super.onCreate()
        // --- HAPUS BARIS INSTANCE = THIS ---
        // instance = this

        // Inisialisasi Firebase (tetap ada)
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
            Log.d("FixItApp", "FirebaseApp Initialized in FixItApplication")
        } else {
            Log.d("FixItApp", "FirebaseApp already initialized.")
        }

        // Inisialisasi Firestore dengan pengaturan kustom
        val firestoreInstance = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
            .build()
        firestoreInstance.firestoreSettings = settings
        Log.d("FixItApp", "Firestore instance configured.")


        // Inisialisasi Google Places API (tetap manual)
        val apiKey = applicationContext.packageManager.getApplicationInfo(
            applicationContext.packageName,
            PackageManager.GET_META_DATA
        ).metaData.getString("com.google.android.geo.API_KEY")

        if (apiKey != null && !Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
            Log.d("FixItApp", "Google Places API Initialized.")
        } else {
            Log.e("FixItApp", "Google Places API Key not found or Places already initialized.")
        }

        // Buat Notification Channel (tetap ada)
        createNotificationChannel()

        // --- HAPUS SEMUA BLOK INISIALISASI DEPENDENSI MANUAL ---
        // 1. Inisialisasi Room Database
        // 2. Dapatkan DAO dari database
        // 3. Inisialisasi Data Source Firebase
        // 4. Inisialisasi Repository
        // 5. Inisialisasi Use Cases
        // --- AKHIR BLOK INISIALISASI MANUAL YANG DIHAPUS ---

        // Jadwalkan Background Task (WorkManager)
        schedulePeriodicSyncWork() // Panggilan ini tetap ada
    }

    override fun onTerminate() {
        super.onTerminate()
        // --- HAPUS PANGGILAN CANCEL SCOPE ---
        // serviceOrderRepositoryImpl.cancelScope()
        // if (this::firebaseServiceOrderDataSource.isInitialized) {
        //     firebaseServiceOrderDataSource.cancelListenerScope()
        // }
    }

    // --- IMPLEMENTASI FUNGSI DARI Configuration.Provider ---
    // Ini memberitahu WorkManager untuk menggunakan HiltWorkerFactory
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory) // <-- Gunakan workerFactory yang di-inject
            .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = Constants.NOTIFICATION_CHANNEL_NAME
            val descriptionText = "Notifikasi untuk update status pesanan FixIT."
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)

            Log.d("FixItApp", "Notification Channel created.")
        }
    }

    private fun schedulePeriodicSyncWork() {
        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES
        )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "FixIT_Periodic_Sync_Work",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )

        Log.d("FixItApp", "Periodic sync work scheduled.")
    }
}