@file:Suppress("REDECLARATION")
package com.xendit.cards_session

import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class XenditCardsSessionPlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var context: Context
  private var apiKey: String? = null
  private val mainScope = CoroutineScope(Dispatchers.Main)

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
        apiKey = call.argument<String>("apiKey")
        if (apiKey.isNullOrEmpty()) {
          result.error("INVALID_API_KEY", "API key cannot be null or empty", null)
          return
        }
        result.success(null)
      }
      "collectCardData" -> {
        if (apiKey.isNullOrEmpty()) {
          result.error("NOT_INITIALIZED", "SDK not initialized. Call initialize() first", null)
          return
        }

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

        // Create request payload
        val requestPayload = JSONObject().apply {
          cardNumber?.let { put("card_number", it) }
          expiryMonth?.let { put("expiry_month", it) }
          expiryYear?.let { put("expiry_year", it) }
          cvn?.let { put("cvn", it) }
          cardholderFirstName?.let { put("cardholder_first_name", it) }
          cardholderLastName?.let { put("cardholder_last_name", it) }
          cardholderEmail?.let { put("cardholder_email", it) }
          cardholderPhoneNumber?.let { put("cardholder_phone_number", it) }
          put("payment_session_id", paymentSessionId)
          put("confirm_save", confirmSave)
          
          // Add device fingerprint (empty for now as per requirements)
          val deviceJson = JSONObject().apply {
            put("fingerprint", "")
          }
          put("device", deviceJson)
        }

        // Make API call using Flutter's platform channel to invoke Dart code
        mainScope.launch {
          try {
            // Prepare the payload on a background thread
            val payload = withContext(Dispatchers.IO) {
              requestPayload.toString()
            }
            
            // Call the API on the main thread
            makeApiRequest("paymentWithSession", payload, result)
          } catch (e: Exception) {
            result.error("API_ERROR", e.message ?: "Unknown error", null)
          }
        }
      }
      "collectCvn" -> {
        if (apiKey.isNullOrEmpty()) {
          result.error("NOT_INITIALIZED", "SDK not initialized. Call initialize() first", null)
          return
        }

        val cvn = call.argument<String>("cvn")
        val paymentSessionId = call.argument<String>("paymentSessionId")

        // Create request payload
        val requestPayload = JSONObject().apply {
          cvn?.let { put("cvn", it) }
          put("payment_session_id", paymentSessionId)
          
          // Add device fingerprint (empty for now as per requirements)
          val deviceJson = JSONObject().apply {
            put("fingerprint", "")
          }
          put("device", deviceJson)
        }

        // Make API call using Flutter's platform channel to invoke Dart code
        mainScope.launch {
          try {
            // Prepare the payload on a background thread
            val payload = withContext(Dispatchers.IO) {
              requestPayload.toString()
            }
            
            // Call the API on the main thread
            makeApiRequest("paymentWithSession", payload, result)
          } catch (e: Exception) {
            result.error("API_ERROR", e.message ?: "Unknown error", null)
          }
        }
      }
      else -> {
        result.notImplemented()
      }
    }
  }

  private fun makeApiRequest(method: String, payload: String, result: Result) {
    // Create a new method channel to call back to Dart
    // This method is called from the main thread
    channel.invokeMethod(
      "makeApiRequest",
      mapOf(
        "method" to method,
        "payload" to payload,
        "apiKey" to apiKey
      ),
      object : Result {
        override fun success(response: Any?) {
          if (response is Map<*, *>) {
            result.success(response)
          } else {
            result.error("INVALID_RESPONSE", "Invalid response format", null)
          }
        }

        override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
          result.error(errorCode, errorMessage, errorDetails)
        }

        override fun notImplemented() {
          result.error("NOT_IMPLEMENTED", "API request method not implemented", null)
        }
      }
    )
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
} 