package com.example.xendit_cards_plugin

import android.content.Context
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import com.xendit.cardsSdk.CardSessions

/** XenditCardsPlugin */
class XenditCardsPlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel: MethodChannel
  private lateinit var cardSessions: CardSessions
  private var context: Context? = null

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "xendit_cards_plugin")
    context = flutterPluginBinding.applicationContext
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    when (call.method) {
      "collectCardData" -> {
        val apiKey = call.argument<String>("apiKey") ?: ""
        if (context == null) {
          result.error("NO_CONTEXT", "Application context is null", null)
          return
        }

        // Initialize CardSessions if not already initialized
        if (!::cardSessions.isInitialized) {
          cardSessions = CardSessions.create(context!!, apiKey)
        }

        // Extract parameters from call
        val cardNumber = call.argument<String>("cardNumber") ?: ""
        val expiryMonth = call.argument<String>("expiryMonth") ?: ""
        val expiryYear = call.argument<String>("expiryYear") ?: ""
        val cvn = call.argument<String>("cvn")
        val firstName = call.argument<String>("cardholderFirstName") ?: ""
        val lastName = call.argument<String>("cardholderLastName") ?: ""
        val email = call.argument<String>("cardholderEmail") ?: ""
        val phoneNumber = call.argument<String>("cardholderPhoneNumber") ?: ""
        val paymentSessionId = call.argument<String>("paymentSessionId") ?: ""
        val confirmSave = call.argument<Boolean>("confirmSave") ?: false

        try {
          cardSessions.collectCardData(
            cardNumber = cardNumber,
            expiryMonth = expiryMonth,
            expiryYear = expiryYear,
            cvn = cvn,
            cardholderFirstName = firstName,
            cardholderLastName = lastName,
            cardholderEmail = email,
            cardholderPhoneNumber = phoneNumber,
            paymentSessionId = paymentSessionId,
            confirmSave = confirmSave
          ).let { response ->
            result.success(response.toString())
          }
        } catch (e: Exception) {
          result.error("COLLECT_CARD_ERROR", e.message, null)
        }
      }

      "collectCvn" -> {
        val apiKey = call.argument<String>("apiKey") ?: ""
        if (context == null) {
          result.error("NO_CONTEXT", "Application context is null", null)
          return
        }

        // Initialize CardSessions if not already initialized
        if (!::cardSessions.isInitialized) {
          cardSessions = CardSessions.create(context!!, apiKey)
        }

        val cvn = call.argument<String>("cvn") ?: ""
        val paymentSessionId = call.argument<String>("paymentSessionId") ?: ""

        try {
          cardSessions.collectCvn(
            cvn = cvn,
            paymentSessionId = paymentSessionId
          ).let { response ->
            result.success(response.toString())
          }
        } catch (e: Exception) {
          result.error("COLLECT_CVN_ERROR", e.message, null)
        }
      }

      else -> {
        result.notImplemented()
      }
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}
