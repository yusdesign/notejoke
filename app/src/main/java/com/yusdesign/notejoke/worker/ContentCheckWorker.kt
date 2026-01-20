package com.yusdesign.notejoke.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.yusdesign.notejoke.data.LoveMeterRepository
import com.yusdesign.notejoke.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class ContentCheckWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, context) {

    private val repository = LoveMeterRepository(context)
    private val notificationHelper = NotificationHelper(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            val latestEntries = repository.fetchLatestEntries()
            if (latestEntries.isNotEmpty()) {
                val latestEntry = latestEntries.first() // Most recent
                val lastNotifiedId = repository.getLastNotifiedId()

                // Show notification ONLY if this is a new, unseen entry
                if (latestEntry.id != lastNotifiedId) {
                    val title = "New Python Love Meter! ❤️"
                    val message = "${latestEntry.status} (${latestEntry.chance}%)"
                    notificationHelper.showNotification(latestEntry.id.hashCode(), title, message)

                    // Save that we've notified for this entry
                    repository.setLastNotifiedId(latestEntry.id)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry() // Try again later if network fails
        }
    }

    companion object {
        // Call this to schedule the periodic check (e.g., from MainActivity)
        fun scheduleWork(context: Context) {
            val checkRequest = PeriodicWorkRequestBuilder<ContentCheckWorker>(
                2, TimeUnit.HOURS // Checks every 2 hours, like the website updates
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "notejoke_content_check",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                checkRequest
            )
        }
    }
}
