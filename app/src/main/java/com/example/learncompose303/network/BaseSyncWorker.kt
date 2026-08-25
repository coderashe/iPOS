package com.example.learncompose303.network

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.learncompose303.app.core.DeviceSession
import com.example.learncompose303.app.core.DeviceSessionInitializer
import com.example.learncompose303.data.repositories.BackfillRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Abstract base for all entity sync workers.
 *
 * Subclasses only need to implement:
 *  - [fetchUnsynced] — query the DAO for dirty rows
 *  - [syncToApi]     — POST a single entity to the backend; return true on 2xx
 *  - [markSynced]    — update isSynced = 1 in the DAO
 *
 * Error classification:
 *  - No network          → Result.retry()
 *  - 4xx per entity      → skip entity, continue batch
 *  - 5xx / IOException   → Result.retry() for entire batch
 */
abstract class BaseSyncWorker(
    context: Context,
    params: WorkerParameters,
    private val connectivityHelper: ConnectivityHelper,
    private val deviceSession: DeviceSession? = null,
    private val deviceSessionInitializer: DeviceSessionInitializer? = null,
    private val backfillRepository: BackfillRepository? = null
) : CoroutineWorker(context, params) {

    companion object {
        const val BATCH_SIZE = 100
        const val KEY_RETRY = "retry"
    }

    protected val TAG: String get() = this::class.java.simpleName

    /** Fetch up to [limit] unsynced rows (isSynced = 0) from the local DB. */
    abstract suspend fun fetchUnsynced(limit: Int): List<Any>

    /**
     * Sync [entity] to the backend.
     * @return true on HTTP 2xx, false on 4xx (skip), throws on 5xx / IOException.
     */
    abstract suspend fun syncToApi(entity: Any): Boolean

    /** Mark [entity] as synced (isSynced = 1) in the local DB. */
    abstract suspend fun markSynced(entity: Any)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val shouldRetry = inputData.getBoolean(KEY_RETRY, true)
        Log.d(TAG, "$TAG — doWork() started (attempt #$runAttemptCount)")
        try {
            val networkAvailable = connectivityHelper.isNetworkAvailable()
            Log.d(TAG, "$TAG — network available: $networkAvailable")
            if (!networkAvailable) {
                Log.w(TAG, "$TAG — no network, retrying")
                return@withContext Result.retry()
            }

            deviceSessionInitializer?.ensureLoaded()

            performBatchSync(shouldRetry)
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "$TAG — cannot reach server: ${e.message}")
            Result.retry()
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "$TAG — timeout: ${e.message}")
            Result.retry()
        } catch (e: java.io.IOException) {
            Log.e(TAG, "$TAG — IOException: ${e.message}")
            Result.retry()
        } catch (e: Exception) {
            Log.e(TAG, "$TAG — unexpected error: ${e.message}", e)
            if (shouldRetry) Result.retry() else Result.failure()
        }
    }

    private suspend fun performBatchSync(shouldRetry: Boolean): Result {
        var totalSynced = 0
        val skipped = mutableSetOf<Any>()
        while (true) {
            val entities = fetchUnsynced(BATCH_SIZE).filter { it !in skipped }
            if (entities.isEmpty()) {
                Log.d(TAG, "$TAG — done (synced: $totalSynced)")
                return Result.success()
            }

            for (entity in entities) {
                try {
                    val ok = syncToApi(entity)
                    if (ok) {
                        markSynced(entity)
                        totalSynced++
                    } else {
                        skipped.add(entity)
                    }
                } catch (e: ClientSyncException) {
                    Log.w(TAG, "$TAG — skipping entity (${e.message})")
                    skipped.add(entity)
                } catch (e: java.io.IOException) {
                    Log.e(TAG, "$TAG — IOException mid-batch, retrying: ${e.message}")
                    return if (shouldRetry) Result.retry() else Result.failure()
                } catch (e: ServerSyncException) {
                    Log.e(TAG, "$TAG — server error mid-batch, retrying: ${e.message}")
                    return if (shouldRetry) Result.retry() else Result.failure()
                }
            }
        }
    }
}

/** Thrown by entity workers when the API returns a 4xx response. */
class ClientSyncException(message: String) : Exception(message)

/** Thrown by entity workers when the API returns a 5xx response. */
class ServerSyncException(message: String) : Exception(message)
