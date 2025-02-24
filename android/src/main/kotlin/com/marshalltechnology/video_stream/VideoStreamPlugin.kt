package com.marshalltechnology.video_stream

import android.app.Activity
import android.os.Build
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.view.TextureRegistry

interface PermissionStuff {
    fun adddListener(listener: PluginRegistry.RequestPermissionsResultListener);
}
///** VideoStreamPlugin */
public class VideoStreamPlugin: FlutterPlugin, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private var methodCallHandler: MethodCallHandlerImpl? = null
    private var flutterPluginBinding: FlutterPlugin.FlutterPluginBinding? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        this.flutterPluginBinding = flutterPluginBinding
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        flutterPluginBinding = null
    }

    private fun maybeStartListening(
        activity: Activity,
        messenger: BinaryMessenger,
        permissionsRegistry: PermissionStuff,
        textureRegistry: TextureRegistry) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // If the sdk is less than 21 (min sdk for Camera2) we don't register the plugin.
            return
        }
        methodCallHandler = MethodCallHandlerImpl(
            activity, messenger, CameraPermissions(), permissionsRegistry, textureRegistry)
    }

    override fun onDetachedFromActivity() {
        if (methodCallHandler == null) {
            return
        }
        methodCallHandler!!.stopListening();
        methodCallHandler = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        maybeStartListening(
            binding.activity,
            flutterPluginBinding!!.binaryMessenger,
            object : PermissionStuff {
                override fun adddListener(listener: PluginRegistry.RequestPermissionsResultListener) {
                    binding.addRequestPermissionsResultListener(listener);
                }

            },

            flutterPluginBinding!!.flutterEngine.renderer
        )
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }
}

