import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revanced.net.revancedmanager.DownloadCompleteEvent
import com.revanced.net.revancedmanager.DownloadEvent
import com.revanced.net.revancedmanager.DownloadProgressEvent
import com.revanced.net.revancedmanager.InstallCompletedEvent
import com.revanced.net.revancedmanager.InstallMainEvent
import com.revanced.net.revancedmanager.OpenAppEvent
import com.revanced.net.revancedmanager.PackageUtils
import com.revanced.net.revancedmanager.RefreshEvent
import com.revanced.net.revancedmanager.UninstallCompletedEvent
import com.revanced.net.revancedmanager.UninstallEvent
import com.revanced.net.revancedmanager.UninstallMainEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.wait
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.security.MessageDigest

/**
 * ViewModel responsible for managing the app list and handling app-related operations
 * Features:
 * - Shows installed apps first
 * - Handles app downloads, installations, and updates
 * - Manages app states and UI updates
 */
class AppViewModel(
    private val context: Context
) : ViewModel() {
    // StateFlow to hold and emit app list updates
    private val _appList = MutableStateFlow<List<AppItem>>(emptyList())
    val appList = _appList.asStateFlow()

    // Downloader instance to handle app downloads
    private val rvDownloader = RvDownloader(context)

    /**
     * ViewModel initialization block
     */
    init {
        // Register for EventBus events
        EventBus.getDefault().register(this)

        // Initial app loading
        viewModelScope.launch {
            try {
                println("Starting initial app load")
                loadAndSortApps(forceRefresh = false)
                println("Initial app load completed")
            } catch (e: Exception) {
                println("Error during initial app load: ${e.message}")
            }
        }
    }

    /**
     * Loads and sorts apps with installed apps appearing first
     * @param forceRefresh Whether to force a refresh from network instead of using cache
     */
    /**
     * Loads and sorts apps with the following priority:
     * 1. First group: Installed apps (UpToDate or UpdateAvailable), sorted by index
     * 2. Second group: All other apps (NotInstalled, UnknownStatus, etc.), sorted by index
     * @param forceRefresh Whether to force a refresh from network instead of using cache
     */
    private suspend fun loadAndSortApps(forceRefresh: Boolean) {
        withContext(Dispatchers.IO) {
            // Get app list
            val unsortedApps = AppItemList.getAppList(context, forceRefresh)

            // Check installation status and sort
            val sortedApps = unsortedApps
                .map { app ->
                val installedVersion = PackageUtils.getInstalledVersion(context, app.packageName)
                var status = calStatus(installedVersion, app)
                app.copy(currentVersion = installedVersion, status = status)
            }
                .sortedWith(
                compareBy<AppItem> { app ->
                    // Primary sort: group by installation status
                    when (app.status) {
                        AppItemStatus.UpToDate,
                        AppItemStatus.UpdateAvailable -> 0  // First group
                        else -> 1  // Second group
                    }
                }.thenBy { app ->
                    // Secondary sort: by index within each group
                    app.index
                }
            )

            // Update the StateFlow with sorted list
            _appList.value = sortedApps
        }

        // If loading from cache, schedule background refresh
        if (!forceRefresh) {
            refreshInBackground()
        }
    }


    override fun onCleared() {
        EventBus.getDefault().unregister(this)
        super.onCleared()
    }




    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDownloadEvent(event: DownloadEvent) {
        println("Download requested for: ${event.packageName}")
        updateAppProgress(event.packageName, 0f)
        updateAppState(event.packageName, AppItemStatus.Downloading)

        val app = _appList.value.firstOrNull { it.packageName == event.packageName }
        val url = app?.downloadUrl

        viewModelScope.launch(Dispatchers.IO) {
            url?.let { rvDownloader.startDownload(event.packageName, it) }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDownloadProgressEvent(event: DownloadProgressEvent) {
        updateAppProgress(event.packageName, event.progress / 100f)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDownloadCompleteEvent(event: DownloadCompleteEvent) {
        updateAppState(event.packageName, AppItemStatus.Installing)
        EventBus.getDefault().post(InstallMainEvent(event.packageName, event.filePath))
    }


    /**
     * Handles refresh event with proper sequencing of operations
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshEvent(event: RefreshEvent) {
        println("onRefreshEvent started")

        viewModelScope.launch {
            try {
                // Clear download table first
                rvDownloader.clearDownloadTable()

                println("Starting app refresh sequence")

                // Create a coroutine scope that waits for all operations
                coroutineScope {
                    // Load apps and wait for completion
                    loadAndSortApps(forceRefresh = true)

                    println("Apps loaded successfully, starting version check")

                    // Now check versions after apps are fully loaded
                    checkAppVersions()
                }

                println("onRefreshEvent completed successfully - all operations done")

            } catch (e: Exception) {
                println("Error during refresh: ${e.message}")
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUninstallEvent(event: UninstallEvent) {
        val packageName = event.packageName
        println("onUninstallEvent: $packageName")
        viewModelScope.launch {
            updateAppProgress(packageName, 0f)
            updateAppState(packageName, AppItemStatus.UnInstalling )
        }
        EventBus.getDefault().post(UninstallMainEvent(packageName = packageName))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onOpenAppEvent(event: OpenAppEvent) {
        val packageName = event.packageName
        println("onOpenAppEvent: $packageName")
        PackageUtils.openAppByPackageName(context, packageName)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUninstallCompletedEvent(event: UninstallCompletedEvent) {
        val packageName = event.packageName
        println("onUninstallCompletedEvent: $packageName, success: ${event.isSuccess}, msg: ${event.msg}")
        viewModelScope.launch {
            when (event.isSuccess) {
                true -> {
                    updateAppState(packageName, AppItemStatus.NotInstalled )
                }
                false -> {
                    checkAppVersions(packageName)
                }
                else -> {
                    //Ignore, it's waiting for user pending action
                }
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onInstallCompletedEvent(event: InstallCompletedEvent) {
        val packageName = event.packageName

        println("onInstallCompletedEvent: $packageName, success: ${event.isSuccess}, msg: ${event.msg}")
        viewModelScope.launch {
            when (event.isSuccess) {
                true -> {
                    updateAppState(packageName, AppItemStatus.UpToDate)
                    checkAppVersions(packageName)
                }
                false -> {
                    updateAppState(packageName, AppItemStatus.NotInstalled)
                    checkAppVersions(packageName)
                }
                null -> {
                    // Installation pending user action
                }
            }
        }

    }


//    private suspend fun AppViewModel.extracted() {
//        loadApps()
//    }


    /**
     * Updates the download progress for a specific app
     * @param packageName The package name of the app
     * @param progress Download progress (0-1)
     */
    private fun updateAppProgress(packageName: String, progress: Float) {
        val updatedList = _appList.value.map { app ->
            if (app.packageName == packageName) {
                app.copy(downloadProgress = progress)
            } else {
                app
            }
        }
//            .sortedWith(compareBy(
//            { it.status == AppItemStatus.NotInstalled },
//            { it.status != AppItemStatus.UpdateAvailable },
//            { it.title.lowercase() }
//        ))
        _appList.value = updatedList
    }


    /**
     * Updates the state of a specific app
     * @param packageName The package name of the app
     * @param status New status for the app
     */
    private fun updateAppState(packageName: String, status: AppItemStatus) {
        val updatedList = _appList.value.map { app ->
            if (app.packageName == packageName) {
                app.copy(status = status)
            } else {
                app
            }
        }
//            .sortedWith(compareBy(
//            { it.status == AppItemStatus.NotInstalled },
//            { it.status != AppItemStatus.UpdateAvailable },
//            { it.title.lowercase() }
//        ))
        _appList.value = updatedList
    }



    /**
     * Load apps from API or cache and ensure all operations complete in sequence
     * @param forceRefresh If true, force reload from API instead of cache
     */
//    private suspend fun loadApps(forceRefresh: Boolean) {
//        println("begin load apps, forceRefresh: $forceRefresh")
//
//        // Use withContext to ensure all operations complete
//        val updatedAppList = withContext(Dispatchers.IO) {
//            // Get app list and wait for completion
//            val appList = AppItemList.getAppList(context, forceRefresh)
//
//            if (!forceRefresh) {
//                // If loading from cache, schedule background refresh
//                println("Schedule background refresh after cache load")
//                launch { refreshInBackground() }
//            }
//
//            appList
//        }
//
//        // Update the state flow with new list
//        _appList.value = updatedAppList
//
//        println("end load apps - all operations completed")
//    }

    /**
     * Performs a background refresh of the app list
     */
    private fun refreshInBackground() {
        viewModelScope.launch(Dispatchers.IO) {
            println("Starting background refresh")
            loadAndSortApps(forceRefresh = true)
            println("Background refresh completed")
        }
    }


    /**
     * Check installed versions of all apps
     */
    fun checkAppVersions() {
        println("Checking app version: begin")

        val updatedList = _appList.value.map { app ->
            val installedVersion = PackageUtils.getInstalledVersion(context, app.packageName)
            val status = when {
                installedVersion == null -> AppItemStatus.NotInstalled
                installedVersion == app.latestVersion -> AppItemStatus.UpToDate
                else -> AppItemStatus.UpdateAvailable
            }
            app.copy(currentVersion = installedVersion, status = status)
        }

        _appList.value = updatedList
        println("Checking app version: finish")
    }



    fun checkAppVersions(packageName: String) {
        println("Checking app version")
        val updatedList = _appList.value.map { app ->
            if (app.packageName == packageName) {
                val installedVersion = PackageUtils.getInstalledVersion(context, app.packageName)
                var status = calStatus(installedVersion, app)
                app.copy(currentVersion = installedVersion, status =  status)
            } else {
                app
            }
        }
        _appList.value = updatedList
    }

    private fun calStatus(installedVersion: String?, app: AppItem): AppItemStatus {
        var status = AppItemStatus.UnknownStatus
        if (installedVersion == null)
            status = AppItemStatus.NotInstalled
        else if (installedVersion == app.latestVersion)
            status = AppItemStatus.UpToDate
        else if (installedVersion != app.latestVersion)
            status = AppItemStatus.UpdateAvailable
        println("${app.title}, installedVersion: $installedVersion, latestVersion: $installedVersion, status: $status")
        return status
    }
}