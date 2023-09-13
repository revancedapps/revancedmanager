package com.revanced.net.revancedmanager

import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import io.flutter.embedding.engine.FlutterEngine
import android.content.Intent
import android.net.Uri

class MainActivity: FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // Create a MethodChannel object and specify the name of the channel that you want to use.
        val channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "com.flutter.uninstall")

        // Register a callback to handle messages that are received from the other side of the channel.
        channel.setMethodCallHandler { call, result ->
            if (call.method == "Uninstall") {
                val packageName = call.argument<String>("package")

                val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE, Uri.parse("package:$packageName"))
                val r =  startActivityForResult(intent,1 )
                result.success(r)
            }
        }
    }
}
