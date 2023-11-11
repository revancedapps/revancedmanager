package com.revanced.net.revancedmanager

import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import io.flutter.embedding.engine.FlutterEngine
import android.content.Intent
import android.net.Uri

class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.flutter.uninstall"

    override fun configureFlutterEngine( flutterEngine: FlutterEngine) {
//        GeneratedPluginRegistrant.registerWith(flutterEngine)
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
            .setMethodCallHandler { call, result ->
                if (call.method.equals("uninstallApp")) {
                    val packageName: String? = call.argument("packageName")
                    if (packageName != null) {
                        uninstallApp(packageName.toString())
                    }

                    result.success(null)
                } else {
                    result.notImplemented()
                }
            }
    }

    private fun uninstallApp(packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE)
        intent.setData(Uri.parse("package:$packageName"))
        startActivity(intent)
    }


}
