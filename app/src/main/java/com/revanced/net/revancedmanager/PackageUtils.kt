package com.revanced.net.revancedmanager

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast

    object PackageUtils {

    fun getInstalledVersion(context: Context, packageName: String): String? {
//        println("getInstalledVersion ${packageName}")
        return try {
            val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
//            println("versionName: ${packageInfo.versionName}")
//            println("packageName: ${packageInfo.packageName}")
//            println("installLocation: ${packageInfo.installLocation}")
//            println("firstInstallTime: ${packageInfo.firstInstallTime}")
//            println("packageInfo: $packageInfo")
            packageInfo.versionName
        } catch (e: Exception) {
//            println("getInstalledVersion err ${packageName}: $e")
            null
        }
    }

    fun openAppByPackageName(context: Context, packageName: String) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                // We found the activity now start the activity
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                // Bring user to the market or let them choose an app?
                val intent = Intent(Intent.ACTION_VIEW)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.data = Uri.parse("market://details?id=$packageName")
                context.startActivity(intent)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            // App not found, show error message
            Toast.makeText(context, "App not installed", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // General error, show error message
            Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show()
        }
    }

}