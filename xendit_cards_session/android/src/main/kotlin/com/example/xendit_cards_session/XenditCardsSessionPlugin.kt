package com.example.xendit_cards_session

import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import android.content.Context

/** XenditCardsSessionPlugin */
class XenditCardsSessionPlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var context: Context

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "xendit_cards_session")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
      "getPlatformVersion" -> {
        result.success("Android ${android.os.Build.VERSION.RELEASE}")
      }
      "initialize" -> {
        val apiKey = call.argument<String>("apiKey")
        // Implementation for initializing the SDK
        result.success(null)
      }
      "collectCardData" -> {
        // Extract parameters from call.arguments
        val cardNumber = call.argument<String>("cardNumber")
        val expiryMonth = call.argument<String>("expiryMonth")
        val expiryYear = call.argument<String>("expiryYear")
        val cvn = call.argument<String>("cvn")
        val cardholderFirstName = call.argument<String>("cardholderFirstName")
        val cardholderLastName = call.argument<String>("cardholderLastName")
        val cardholderEmail = call.argument<String>("cardholderEmail")
        val cardholderPhoneNumber = call.argument<String>("cardholderPhoneNumber")
        val paymentSessionId = call.argument<String>("paymentSessionId")
        val confirmSave = call.argument<Boolean>("confirmSave") ?: false

        // Mock implementation - return a sample success response
        val mockResponse = mapOf(
          "message" to "Status updated. Wait for callback",
          "payment_token_id" to "pt-mock-12345",
          "action_url" to "https://redirect-gateway.example.com"
        )
        result.success(mockResponse)
      }
      "collectCvn" -> {
        val cvn = call.argument<String>("cvn")
        val paymentSessionId = call.argument<String>("paymentSessionId")

        // Mock implementation - return a sample success response
        val mockResponse = mapOf(
          "message" to "CVN collected successfully",
          "payment_token_id" to "pt-mock-12345"
        )
        result.success(mockResponse)
      }
      else -> {
        result.notImplemented()
      }
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}
