import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URL

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class AppItem(
    val title: String,                // Application name
    val description: String,          // Application description
    val packageName: String,          // Android package name
    val currentVersion: String? = null,   // Currently installed version
    val latestVersion: String,        // Latest available version from server
    val downloadUrl: String,          // APK download URL
    val logo: String,                 // App icon URL
    val index: Int,                 // App index
    var downloadProgress: Float = 0f,  // Download progress (0-1)
    var status: AppItemStatus = AppItemStatus.UnknownStatus
) {
    companion object {
        /**
         * Converts JsonAppPackage to AppItem
         */
        fun fromJsonPackage(pkg: JsonAppPackage): AppItem {
            return AppItem(
                title = pkg.appName,
                description = pkg.appShortDescription,
                packageName = pkg.androidPackageName,
                latestVersion = pkg.latestVersionCode,
                downloadUrl = pkg.latestVersionUrl,
                logo = pkg.icon,
                index = pkg.index
            )
        }
    }
}

enum class AppItemStatus {
    UnknownStatus,
    NotInstalled,
    UpToDate,
    UpdateAvailable,
    PendingDownload,
    Downloading,
    Installing,
    UnInstalling,
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class JsonAppPackage(
    val appName: String,
    val androidPackageName: String,
    val latestVersionCode: String,
    val appShortDescription: String,
    val requireMicroG: Boolean,
    val latestVersionUrl: String,
    val icon: String,
    val index: Int,
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class JsonAppResponse(
    val packages: List<JsonAppPackage>,
    val sponsor: String? = null
)

/**
 * Handles fetching and managing app list data
 */
object AppItemList {
    private const val BASE_URL = "https://rv.net/"
    private const val CACHED_APP_LIST_KEY = "cached_app_list"

    // API endpoints for different architectures
    private const val ARM64_V8A_URL = "${BASE_URL}rv-apps-arm64-v8a.json"
    private const val ARMEABI_V7A_URL = "${BASE_URL}rv-apps-armeabi-v7a.json"
    private const val X86_URL = "${BASE_URL}rv-apps-x86.json"
    private const val X86_64_URL = "${BASE_URL}rv-apps-x86_64.json"
    private const val FALLBACK_URL = "${BASE_URL}rv-apps.json"

    /**
     * Get list of supported CPU architectures
     */
    private fun getSupportedAbis(): Array<String> {
        return try {
            Build.SUPPORTED_ABIS
        } catch (e: Exception) {
            println("Error getting supported ABIs: ${e.message}")
            emptyArray()
        }
    }

    /**
     * Get appropriate API URL based on device architecture
     */
    private fun getApiUrl(): String {
        val supportedAbis = getSupportedAbis()
        println("Supported ABIs: ${supportedAbis.joinToString()}")

        return when {
            supportedAbis.contains("arm64-v8a") -> ARM64_V8A_URL
            supportedAbis.contains("armeabi-v7a") -> ARMEABI_V7A_URL
            supportedAbis.contains("x86") -> X86_URL
            supportedAbis.contains("x86_64") -> X86_64_URL
            else -> FALLBACK_URL
        }
    }

    /**
     * Fetch app list from server or cache
     */
    suspend fun getAppList(context: Context, forceRefresh: Boolean = false): List<AppItem> = withContext(Dispatchers.IO) {
        println("Getting app list, forceRefresh: $forceRefresh")

        if (!forceRefresh) {
            // Try loading from cache first
            loadCachedAppList(context)?.let {
                return@withContext it
            }
        }

        // Determine API URL based on device architecture
        val apiUrl = getApiUrl()
        println("Using API URL: $apiUrl")

        try {
            // Fetch and parse JSON from API
            val jsonString = URL(apiUrl).readText()
            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
            val response = json.decodeFromString<JsonAppResponse>(jsonString)

            // Convert to AppItems
            val appList = response.packages.map { pkg ->
                AppItem.fromJsonPackage(pkg)
            }

            // Cache the new list
            cacheAppList(context, appList)

            println("Successfully fetched ${appList.size} apps")
            appList

        } catch (e: Exception) {
            println("Error fetching app list: ${e.message}")
            emptyList()
        }
    }

    /**
     * Load app list from cache
     */
    private fun loadCachedAppList(context: Context): List<AppItem>? {
        println("Loading app list from cache")
        val jsonString = SharedPreferencesUtil.getString(CACHED_APP_LIST_KEY, "")
        return if (jsonString.isNotEmpty()) {
            Json.decodeFromString<List<AppItem>>(jsonString)
        } else {
            null
        }
    }

    /**
     * Save app list to cache
     */
    private fun cacheAppList(context: Context, appList: List<AppItem>) {
        println("Caching ${appList.size} apps")
        val jsonString = Json.encodeToString(appList)
        SharedPreferencesUtil.saveString(CACHED_APP_LIST_KEY, jsonString)
    }
}