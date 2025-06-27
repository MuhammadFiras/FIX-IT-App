package com.example.fixit.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fixit.app.FixItApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.work.ListenableWorker // <-- PASTIKAN INI DIIMPOR

class SyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): ListenableWorker.Result {
        Log.d("SyncWorker", "Starting periodic sync work...")

        val application = applicationContext as FixItApplication
        val serviceOrderUseCases = application.serviceOrderUseCases

        return withContext(Dispatchers.IO) {
            try {
                val result = serviceOrderUseCases.syncAllOrdersFromFirebaseToRoom() // ← pastikan pakai ()

                if (result.isSuccess) {
                    Log.d("SyncWorker", "Periodic sync work succeeded.")
                    ListenableWorker.Result.success()
                } else {
                    Log.e("SyncWorker", "Periodic sync work failed: ${result.exceptionOrNull()}")
                    ListenableWorker.Result.failure()
                }
            } catch (e: Exception) {
                Log.e("SyncWorker", "Exception during sync: ${e.message}", e)
                ListenableWorker.Result.failure()
            }
        }
    }
}