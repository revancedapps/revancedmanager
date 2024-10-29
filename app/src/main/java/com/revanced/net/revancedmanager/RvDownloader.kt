import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import com.revanced.net.revancedmanager.DownloadCompleteEvent
import com.revanced.net.revancedmanager.DownloadEvent
import com.revanced.net.revancedmanager.DownloadProgressEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.greenrobot.eventbus.EventBus
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/**
 * RvDownloader: A class responsible for managing downloads in the ReVanced Manager app.
 * It handles download initialization, monitoring, and status updates.
 *
 * @property context The application context used for system service access.
 */
class RvDownloader(private val context: Context) {
    // Android's DownloadManager service for handling downloads
    private val downloadManager: DownloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    // A map to store information about ongoing and completed downloads
    private val downloadInfoTable = mutableMapOf<String, DownloadInfo>()

    init {
        loadDownloadInfoFromPreferences()
        registerDownloadReceiver()
    }

    /**
     * DownloadInfo: Data class to store information about a download.
     * This class is serializable to allow easy storage in SharedPreferences.
     */
    @Serializable
    private data class DownloadInfo(
        val downloadId: Long,
        val url: String,
        var filePath: String? = null,
        var status: DownloadStatus = DownloadStatus.IN_PROGRESS,
        var timestamp: Long = System.currentTimeMillis()
    )

    /**
     * DownloadStatus: Enum to represent the current status of a download.
     */
    private enum class DownloadStatus {
        IN_PROGRESS, COMPLETED
    }

    /**
     * Loads download information from SharedPreferences.
     * This allows persistence of download info across app restarts.
     */
    private fun loadDownloadInfoFromPreferences() {
        val savedInfo = SharedPreferencesUtil.getString("download_info_table", "")
        if (savedInfo.isNotEmpty()) {
            val infoMap: Map<String, DownloadInfo> = Json.decodeFromString(savedInfo)
            downloadInfoTable.putAll(infoMap)
        }
    }

    /**
     * Saves current download information to SharedPreferences.
     * This should be called whenever the downloadInfoTable is modified.
     */
    private fun saveDownloadInfoToPreferences() {
        val infoString = Json.encodeToString(downloadInfoTable)
        SharedPreferencesUtil.saveString("download_info_table", infoString)
    }

    /**
     * Clears all stored download information and cancels ongoing downloads.
     * This can be used for cleanup or when the user wants to reset all download statuses.
     */
    fun clearDownloadTable() {
        downloadInfoTable.clear()
        saveDownloadInfoToPreferences()
        // Cancel all ongoing downloads
        downloadInfoTable.values.forEach { downloadInfo ->
            downloadManager.remove(downloadInfo.downloadId)
        }
    }

    /**
     * Registers a BroadcastReceiver to listen for download completion events.
     */
    private fun registerDownloadReceiver() {
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(downloadReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(downloadReceiver, filter)
        }
    }

    /**
     * BroadcastReceiver to handle download completion events.
     */
    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            println("downloadReceiver: ${intent.action}")
            if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                val packageName = getPackageNameForDownloadId(id)
                if (packageName != null) {
                    checkDownloadStatus(packageName)
                }
            }
        }
    }

    /**
     * Retrieves the package name associated with a given download ID.
     */
    private fun getPackageNameForDownloadId(downloadId: Long): String? {
        return downloadInfoTable.entries.find { it.value.downloadId == downloadId }?.key
    }

    /**
     * Checks the status of a download and updates the downloadInfoTable accordingly.
     */
    private fun checkDownloadStatus(packageName: String) {
        val downloadInfo = downloadInfoTable[packageName] ?: return
        val query = DownloadManager.Query().setFilterById(downloadInfo.downloadId)
        val cursor = downloadManager.query(query)

        if (cursor.moveToFirst()) {
            val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    val localUri =
                        cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                    val filePath = getFilePathFromUri(localUri)
                    downloadInfo.filePath = filePath
                    downloadInfo.status = DownloadStatus.COMPLETED
                    downloadInfo.timestamp = System.currentTimeMillis()
                    EventBus.getDefault().post(DownloadCompleteEvent(packageName, filePath!!))
                    saveDownloadInfoToPreferences()
                }

                DownloadManager.STATUS_FAILED -> {
                    EventBus.getDefault().post(DownloadProgressEvent(packageName, -1, 0, 0))
                    downloadInfoTable.remove(packageName)
                    saveDownloadInfoToPreferences()
                }
            }
        }
        cursor.close()
    }

    /**
     * Initiates or resumes a download for a given package.
     *
     * @param packageName The name of the package to download.
     * @param downloadUrl The URL to download the package from.
     * @return The ID of the initiated or resumed download.
     */
    suspend fun startDownload(packageName: String, downloadUrl: String): Long =
        withContext(Dispatchers.IO) {
            val existingDownload = downloadInfoTable[packageName]
            if (existingDownload != null) {
                when {
                    // If a completed, recent download with the same URL exists, use it
                    existingDownload.status == DownloadStatus.COMPLETED &&
                            isDownloadRecent(existingDownload.timestamp) &&
                            existingDownload.url == downloadUrl -> {
                        EventBus.getDefault().post(DownloadCompleteEvent(packageName, existingDownload.filePath!!))
                        return@withContext existingDownload.downloadId
                    }
                    // If a download is in progress with the same URL, continue monitoring it
                    existingDownload.status == DownloadStatus.IN_PROGRESS &&
                            existingDownload.url == downloadUrl -> {
                        monitorDownloadProgress(existingDownload.downloadId, packageName)
                        return@withContext existingDownload.downloadId
                    }
                    // Otherwise, remove the old download info and start a new download
                    else -> {
                        downloadManager.remove(existingDownload.downloadId)
                        downloadInfoTable.remove(packageName)
                    }
                }
            }

            // Start a new download
            val hash = md5Hash(downloadUrl)
            val fileName = "${packageName}_$hash.apk"

            val request = DownloadManager.Request(Uri.parse(downloadUrl))
                .setTitle("Downloading $packageName")
                .setDescription(packageName)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(
                    context,
                    Environment.DIRECTORY_DOWNLOADS,
                    fileName
                )

            val downloadId = downloadManager.enqueue(request)
            downloadInfoTable[packageName] = DownloadInfo(
                downloadId,
                downloadUrl
            )
            saveDownloadInfoToPreferences()

            EventBus.getDefault().post(DownloadEvent(packageName))

            monitorDownloadProgress(downloadId, packageName)

            return@withContext downloadId
        }

    /**
     * Monitors the progress of a download and posts updates via EventBus.
     */
    private suspend fun monitorDownloadProgress(downloadId: Long, packageName: String) =
        withContext(Dispatchers.IO) {
            var isDownloading = true
            while (isDownloading) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)

                if (cursor.moveToFirst()) {
                    val bytesDownloaded =
                        cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val bytesTotal =
                        cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                    if (bytesTotal > 0) {
                        val progress = (bytesDownloaded * 100 / bytesTotal).toInt()
                        EventBus.getDefault().post(
                            DownloadProgressEvent(
                                packageName,
                                progress,
                                bytesDownloaded,
                                bytesTotal
                            )
                        )
                    }

                    val status =
                        cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        isDownloading = false
                        val localUri =
                            cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                        val filePath = getFilePathFromUri(localUri)
                        updateDownloadInfo(packageName, filePath, DownloadStatus.COMPLETED)
                        EventBus.getDefault().post(DownloadCompleteEvent(packageName, filePath!!))
                    } else if (status == DownloadManager.STATUS_FAILED) {
                        isDownloading = false
                        EventBus.getDefault().post(DownloadProgressEvent(packageName, -1, 0, 0))
                        downloadInfoTable.remove(packageName)
                        saveDownloadInfoToPreferences()
                    }
                }

                cursor.close()
                if (isDownloading) {
                    delay(500) // Wait for 500ms before checking again
                }
            }
        }

    /**
     * Updates the download information for a given package.
     */
    private fun updateDownloadInfo(packageName: String, filePath: String?, status: DownloadStatus) {
        downloadInfoTable[packageName]?.let { info ->
            info.filePath = filePath
            info.status = status
            info.timestamp = System.currentTimeMillis()
            saveDownloadInfoToPreferences()
        }
    }

    /**
     * Extracts the file path from a URI string.
     */
    private fun getFilePathFromUri(uriString: String): String? {
        val uri = Uri.parse(uriString)
        return uri.path
    }

    /**
     * Generates an MD5 hash for a given input string.
     * Used to create unique filenames for downloads.
     */
    private fun md5Hash(input: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Checks if a download is recent (within the last 24 hours).
     */
    private fun isDownloadRecent(timestamp: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - timestamp
        return timeDifference <= TimeUnit.HOURS.toMillis(24)
    }
}