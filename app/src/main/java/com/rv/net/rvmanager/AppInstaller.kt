import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.os.Build
import android.app.PendingIntent
import com.rv.net.rvmanager.InstallCompletedEvent
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class AppInstaller(private val context: Context) {
    private val installReceiver = InstallReceiver()
    private val ACTION_INSTALL_RESULT = "${context.packageName}.INSTALL_RESULT"

    init {
        val filter = IntentFilter(ACTION_INSTALL_RESULT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(installReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(installReceiver, filter)
        }
    }

    /**
     * Installs an APK file using PackageInstaller
     *
     * @param packageName Package name for tracking and event handling
     * @param apkPath Full path to the APK file to be installed
     */
    fun installApp(packageName: String, apkPath: String) {
        println("Starting installation for APK: $apkPath")
        val packageInstaller = context.packageManager.packageInstaller

        try {
            // Create installation session parameters
            val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            // Note: Setting package name is optional, removing it since the APK contains this information

            // Create and open installation session
            val sessionId = packageInstaller.createSession(params)
            val session = packageInstaller.openSession(sessionId)

            // Write APK file to session
            session.use { activeSession ->
                FileInputStream(File(apkPath)).use { inputStream ->
                    // Open write stream with a generic name since package name isn't needed
                    activeSession.openWrite("package", 0, -1).use { outputStream ->
                        // Copy APK data to installation session
                        inputStream.copyTo(outputStream)
                        // Ensure all data is written
                        activeSession.fsync(outputStream)
                    }
                }

                // Create intent for installation result
                val intent = Intent(ACTION_INSTALL_RESULT).apply {
                    setPackage(context.packageName)
                    // Keep package name in extra for tracking purposes
                    putExtra("PACKAGE_NAME", packageName)
                }

                // Create appropriate PendingIntent based on Android version
                val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.getBroadcast(
                        context,
                        sessionId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                } else {
                    PendingIntent.getBroadcast(
                        context,
                        sessionId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }

                // Start the installation
                activeSession.commit(pendingIntent.intentSender)
            }
        } catch (e: IOException) {
            println("Installation failed: ${e.message}")
            // Notify about installation failure
            EventBus.getDefault().post(InstallCompletedEvent(
                packageName = packageName,
                isSuccess = false,
                msg = "Installation failed: ${e.message}"
            ))
        }
    }

    fun cleanup() {
        context.unregisterReceiver(installReceiver)
    }

    inner class InstallReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val packageName = intent.getStringExtra("PACKAGE_NAME") ?: "Unknown package"
            val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)
            val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE) ?: "Unknown reason"

            when (status) {
                PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                    val confirmIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(Intent.EXTRA_INTENT)
                    }
                    confirmIntent?.let { context.startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }

                    EventBus.getDefault().post(InstallCompletedEvent(packageName, null, "Status: $status, Message: $message"))
                }
                PackageInstaller.STATUS_SUCCESS -> {
                    ToastUtil.showSmallToast(context, "Installed: $packageName", android.widget.Toast.LENGTH_SHORT)
                    EventBus.getDefault().post(InstallCompletedEvent(packageName, true, "Status: $status, Message: $message"))
                }
                else -> {
                    ToastUtil.showSmallToast(context, "$message. Installation failed: $packageName", android.widget.Toast.LENGTH_SHORT)
                    EventBus.getDefault().post(InstallCompletedEvent(packageName, false, "Status: $status, Message: $message"))
                }
            }
        }
    }
}