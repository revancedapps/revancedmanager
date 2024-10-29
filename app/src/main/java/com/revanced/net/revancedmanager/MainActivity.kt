package com.revanced.net.revancedmanager

import AppInstaller
import AppUninstaller
import RevancedManagerApp
import RevancedManagerTheme
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.EventBus

class MainActivity : ComponentActivity() {
    private lateinit var appUninstaller: AppUninstaller
    private lateinit var appInstaller: AppInstaller

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        SharedPreferencesUtil.init(this)

        // Make the app full screen
        WindowCompat.setDecorFitsSystemWindows(window, false)
//        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
//            controller.hide(WindowInsetsCompat.Type.systemBars())
//            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//        }

        setContent {
            RevancedManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RevancedManagerApp(context = baseContext)
                }
            }
        }
        appUninstaller = AppUninstaller(this)
        appInstaller = AppInstaller(this)


    }



    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        appUninstaller.cleanup()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUninstallMainEvent(event: UninstallMainEvent) {
        println("onUninstallMainEvent")
        // Start the uninstallation process
        val packageName = event.packageName
        appUninstaller.uninstallApp(packageName)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onInstallMainEvent(event: InstallMainEvent) {
        println("onInstallMainEvent: ${event.packageName}")
        appInstaller.installApp(event.packageName, event.apkFilePath)
    }
}



