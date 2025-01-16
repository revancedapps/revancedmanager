import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rv.net.rvmanager.DownloadCompleteEvent
import com.rv.net.rvmanager.DownloadEvent
import com.rv.net.rvmanager.DownloadProgressEvent
import com.rv.net.rvmanager.InstallCompletedEvent
import com.rv.net.rvmanager.InstallMainEvent
import com.rv.net.rvmanager.OpenAppEvent
import com.rv.net.rvmanager.PackageUtils
import com.rv.net.rvmanager.RefreshEvent
import com.rv.net.rvmanager.UninstallCompletedEvent
import com.rv.net.rvmanager.UninstallEvent
import com.rv.net.rvmanager.UninstallMainEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

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
                    val installedVersion =
                        PackageUtils.getInstalledVersion(context, app.packageName)
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
//                rvDownloader.clearDownloadTable()

                println("Starting app refresh sequence")
                ToastUtil.showSmallToast(context, "Refresh...")
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
            updateAppState(packageName, AppItemStatus.UnInstalling)
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
                    updateAppState(packageName, AppItemStatus.NotInstalled)
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
        println("Starting version check for all apps")

        val updatedList = _appList.value.map { app ->
            val installedVersion = PackageUtils.getInstalledVersion(context, app.packageName)
            val newStatus = calStatus(installedVersion, app)
            app.copy(currentVersion = installedVersion, status = newStatus)
        }

        _appList.value = updatedList
        println("Version check completed for all apps")
    }


    fun checkAppVersions(packageName: String) {
        println("Checking version for package: $packageName")

        val updatedList = _appList.value.map { app ->
            if (app.packageName == packageName) {
                val installedVersion = PackageUtils.getInstalledVersion(context, app.packageName)
                val newStatus = calStatus(installedVersion, app)
                app.copy(currentVersion = installedVersion, status = newStatus)
            } else {
                app
            }
        }

        _appList.value = updatedList
    }

    /**
     * Compares installed version against a target version
     * @param installedVersion Currently installed version (e.g. "2.0.7")
     * @param comparedVersion Version to compare against (e.g. "2.0.6")
     * @return Positive if installedVersion > comparedVersion
     *         Negative if installedVersion < comparedVersion
     *         Zero if versions are equal or comparison fails
     */
    private fun compareVersions(installedVersion: String, comparedVersion: String): Int {
        // Handle empty cases safely
        if (installedVersion.isEmpty() || comparedVersion.isEmpty()) {
            return 0
        }

        try {
            // Split versions into components and clean non-numeric characters
            val installedParts = installedVersion.split(".").map { part ->
                // Extract only numeric portion from each part (e.g. "2beta" -> "2")
                part.takeWhile { it.isDigit() }.ifEmpty { "0" }
            }
            val comparedParts = comparedVersion.split(".").map { part ->
                part.takeWhile { it.isDigit() }.ifEmpty { "0" }
            }

            // Get the length of the shorter version
            val length = minOf(installedParts.size, comparedParts.size)

            // Compare each component
            for (i in 0 until length) {
                // Convert to Long to handle larger numbers, defaulting to 0 if conversion fails
                val installedNum = installedParts[i].toLongOrNull() ?: 0L
                val comparedNum = comparedParts[i].toLongOrNull() ?: 0L

                when {
                    installedNum > comparedNum -> return 1
                    installedNum < comparedNum -> return -1
                }
            }

            // If all common components are equal, longer version is considered newer
            return installedParts.size.compareTo(comparedParts.size)

        } catch (e: Exception) {
            // Log error and return 0 to indicate versions are considered equal
            println("Version comparison failed: ${e.message}")
            println("installedVersion: $installedVersion, comparedVersion: $comparedVersion")
            return 0
        }
    }

    /**
     * Determines the installation status of an app based on version comparison
     * @param installedVersion Currently installed version (can be null if not installed)
     * @param app The app item containing latest version information
     * @return AppItemStatus representing the current status of the app
     */
    private fun calStatus(installedVersion: String?, app: AppItem): AppItemStatus {
        return when {
            // App is not installed
            installedVersion == null -> AppItemStatus.NotInstalled

            // Compare versions to determine status
            compareVersions(installedVersion, app.latestVersion) >= 0 -> {
                // Installed version is newer or equal to latest
                AppItemStatus.UpToDate
            }
            else -> {
                // Installed version is older than latest
                AppItemStatus.UpdateAvailable
            }
        }.also { status ->
            // Log the status calculation for debugging
            println("App: ${app.title}, Installed: $installedVersion, Latest: ${app.latestVersion}, Status: $status")
        }
    }
}