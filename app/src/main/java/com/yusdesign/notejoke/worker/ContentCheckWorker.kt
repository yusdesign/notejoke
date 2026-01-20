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
            // Try fetching from the result.txt directly first
            val resultTxtUrl = "https://raw.githubusercontent.com/yusdesign/lovepy/main/result.txt"
            val latestEntries = fetchFromRawGithub(resultTxtUrl)
            
            if (latestEntries.isEmpty()) {
                // Fallback to the HTML page
                latestEntries = repository.fetchLatestEntries()
            }
            
            if (latestEntries.isNotEmpty()) {
                val latestEntry = latestEntries.first()
                val lastNotifiedId = repository.getLastNotifiedId()
                
                if (latestEntry.id != lastNotifiedId) {
                    // Show notification
                    notificationHelper.showNotification(
                        notificationId = latestEntry.id.hashCode(),
                        title = "New Love Meter Result!",
                        message = "${latestEntry.status} (${latestEntry.chance}%)"
                    )
                    
                    repository.setLastNotifiedId(latestEntry.id)
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    private suspend fun fetchFromRawGithub(url: String): List<LoveMeter> {
        return withContext(Dispatchers.IO) {
            try {
                val response = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .timeout(10000)
                    .execute()
                    .body()
                
                parseResultTxt(response)
            } catch (e: Exception) {
                emptyList()
            }
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
