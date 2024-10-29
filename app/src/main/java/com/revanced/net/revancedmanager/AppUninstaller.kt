import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.widget.Toast
import android.app.PendingIntent
import android.os.Build
import com.revanced.net.revancedmanager.UninstallCompletedEvent
import org.greenrobot.eventbus.EventBus

class AppUninstaller(private val context: Context) {
    private val uninstallReceiver = UninstallReceiver()
    private val ACTION_UNINSTALL_RESULT = "${context.packageName}.UNINSTALL_RESULT"

    init {
        val filter = IntentFilter(ACTION_UNINSTALL_RESULT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(uninstallReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(uninstallReceiver, filter)
        }
    }

    fun uninstallApp(packageName: String) {
        val packageInstaller = context.packageManager.packageInstaller

        val intent = Intent(ACTION_UNINSTALL_RESULT).apply {
            setPackage(context.packageName)
            putExtra("PACKAGE_NAME", packageName)
        }
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                context,
                packageName.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getBroadcast(
                context,
                packageName.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        packageInstaller.uninstall(packageName, pendingIntent.intentSender)
    }

    fun cleanup() {
        context.unregisterReceiver(uninstallReceiver)
    }

    inner class UninstallReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val packageName = intent.getStringExtra("PACKAGE_NAME") ?: "Unknown package"
            var status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)
            val reason = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE) ?: "Unknown reason"
            when (status) {
                PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                    val confirmIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(Intent.EXTRA_INTENT)
                    }
                    confirmIntent?.let { context.startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }

                    EventBus.getDefault().post(UninstallCompletedEvent(packageName, null, "Status: $status, Reason: $reason"))
                }
                PackageInstaller.STATUS_SUCCESS -> {
                    ToastUtil.showSmallToast(context, "Uninstalled: $packageName", Toast.LENGTH_SHORT)
                    EventBus.getDefault().post(UninstallCompletedEvent(packageName, true, "Status: $status, Reason: $reason"))
                }
                PackageInstaller.STATUS_FAILURE,
                PackageInstaller.STATUS_FAILURE_ABORTED,
                PackageInstaller.STATUS_FAILURE_BLOCKED,
                PackageInstaller.STATUS_FAILURE_CONFLICT,
                PackageInstaller.STATUS_FAILURE_INCOMPATIBLE,
                PackageInstaller.STATUS_FAILURE_INVALID,
                PackageInstaller.STATUS_FAILURE_STORAGE -> {
                    ToastUtil.showSmallToast(context, "$reason. Uninstall failed: $packageName", Toast.LENGTH_SHORT)
                    EventBus.getDefault().post(UninstallCompletedEvent(packageName, false, "Status: $status, Reason: $reason"))
                }
                else -> {
                    ToastUtil.showSmallToast(context, "$reason, package: $packageName , status: $status", Toast.LENGTH_SHORT)
                    EventBus.getDefault().post(UninstallCompletedEvent(packageName, false, "Status: $status, Reason: $reason"))
                }
            }


        }
    }
}
