package com.example.fixit.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.hilt.work.HiltWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.example.fixit.domain.usecase.ServiceOrderUseCases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.work.ListenableWorker


@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val serviceOrderUseCases: ServiceOrderUseCases
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("SyncWorker", "Starting periodic sync work...")

        return withContext(Dispatchers.IO) {
            try {
                val kotlinResult = serviceOrderUseCases.syncAllOrdersFromFirebaseToRoom()

                if (kotlinResult.isSuccess) {
                    Log.d("SyncWorker", "Periodic sync work succeeded.")
                    Result.success()
                } else {
                    val errorMessage = kotlinResult.exceptionOrNull()?.message ?: "Unknown error"
                    Log.e("SyncWorker", "Periodic sync work failed: $errorMessage", kotlinResult.exceptionOrNull())
                    Result.failure()
                }
            } catch (e: Exception) {
                Log.e("SyncWorker", "Exception during sync: ${e.message}", e)
                Result.failure()
            }
        }
    }
}