@file:Suppress("REDECLARATION")
package com.xendit.cards_session

import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result as FlutterResult // otherwise it gets confused with another Result class
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import com.xendit.fingerprintsdk.XenditFingerprintSDK
import kotlin.coroutines.resume

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

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: FlutterResult) {
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
        
        try {
          XenditFingerprintSDK.init(context, apiKey!!)
        } catch (e: Exception) {
          // Don't return error as the main SDK can still function without fingerprinting
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
        val billingInformation = call.argument<Map<String, Any?>>("billingInformation")

        mainScope.launch {
          try {
            val fingerprint = getFingerprint("collect_card_data")
            
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

              // Add billing information if present
              billingInformation?.let {
                val billingJson = JSONObject().apply {
                  it["first_name"]?.let { v -> put("first_name", v as String) }
                  it["last_name"]?.let { v -> put("last_name", v as String) }
                  it["email"]?.let { v -> put("email", v as String) }
                  it["phone_number"]?.let { v -> put("phone_number", v as String) }
                  it["street_line1"]?.let { v -> put("street_line1", v as String) }
                  it["street_line2"]?.let { v -> put("street_line2", v as String) }
                  it["city"]?.let { v -> put("city", v as String) }
                  it["province_state"]?.let { v -> put("province_state", v as String) }
                  it["country"]?.let { v -> put("country", v as String) }
                  it["postal_code"]?.let { v -> put("postal_code", v as String) }
                }
                put("billing_information", billingJson)
              }

              val deviceJson = JSONObject().apply {
                put("fingerprint", fingerprint)
              }
              put("device", deviceJson)
            }

            // Call the API directly on the main thread
            makeApiRequest("paymentWithSession", requestPayload.toString(), result)
          } catch (e: Exception) {
            result.error("API_ERROR", e.message ?: "Unknown error", null)
          }
        }
      }
      "collectCvn" -> {
        if (apiKey.isNullOrEmpty()) {
          result.error("NOT_INITIALIZED", "SDK not initialized. Call initialize() with valid API key first", null)
          return
        }

        val cvn = call.argument<String>("cvn")
        val paymentSessionId = call.argument<String>("paymentSessionId")

        mainScope.launch {
          try {
            val fingerprint = getFingerprint("collect_cvn")
            
            // Create request payload
            val requestPayload = JSONObject().apply {
              cvn?.let { put("cvn", it) }
              put("payment_session_id", paymentSessionId)
              
              val deviceJson = JSONObject().apply {
                put("fingerprint", fingerprint)
              }
              put("device", deviceJson)
            }

            // Call the API directly on the main thread
            makeApiRequest("paymentWithSession", requestPayload.toString(), result)
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

  private suspend fun getFingerprint(eventName: String): String {
    return try {
      val sessionId = XenditFingerprintSDK.getSessionId()
      
      // Create a kotlinx.coroutines compatible wrapper for the callback-based XenditFingerprintSDK.scan
      suspendCancellableCoroutine { continuation ->
        try {
          XenditFingerprintSDK.scan(
            customerEventName = eventName,
            customerEventID = sessionId,
            onSuccess = {
              continuation.resume(sessionId)
            },
            onError = { error ->
              // Even if there's an error, we still return the sessionId
              continuation.resume(sessionId)
            }
          )
        } catch (e: Exception) {
          continuation.resume("") // Return empty string on failure
        }
      }
    } catch (e: Exception) {
      "" // Return empty string on failure
    }
  }

  private fun makeApiRequest(method: String, payload: String, result: FlutterResult) {
    // Create a new method channel to call back to Dart
    // This method is called from the main thread
    channel.invokeMethod(
      "makeApiRequest",
      mapOf(
        "method" to method,
        "payload" to payload,
        "apiKey" to apiKey
      ),
      object : FlutterResult {
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