import Flutter
import UIKit

#if canImport(XenditFingerprintSDK)
import XenditFingerprintSDK
#endif

public class XenditCardsSessionPlugin: NSObject, FlutterPlugin {
  private var channel: FlutterMethodChannel?
  private var apiKey: String?
  
  #if canImport(XenditFingerprintSDK)
  private var fingerprintSDK: FingerprintSDK?
  #endif
  
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "xendit_cards_session", binaryMessenger: registrar.messenger())
    let instance = XenditCardsSessionPlugin()
    instance.channel = channel
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
    case "getPlatformVersion":
      result("iOS " + UIDevice.current.systemVersion)
    case "initialize":
      guard let args = call.arguments as? [String: Any],
            let apiKey = args["apiKey"] as? String else {
        result(FlutterError(code: "INVALID_ARGUMENTS", message: "Missing apiKey", details: nil))
        return
      }
      
      self.apiKey = apiKey
      
      // Initialize the fingerprint SDK if available
      #if canImport(XenditFingerprintSDK)
      do {
        self.fingerprintSDK = FingerprintSDK()
        try self.fingerprintSDK?.initSDK(apiKey: apiKey)
      } catch {
        self.fingerprintSDK = nil
      }
      #endif
      
      result(nil)
    case "collectCardData":
      guard let apiKey = self.apiKey else {
        result(FlutterError(code: "NOT_INITIALIZED", message: "SDK not initialized. Call initialize() first", details: nil))
        return
      }
      
      guard let args = call.arguments as? [String: Any],
            let cardNumber = args["cardNumber"] as? String,
            let expiryMonth = args["expiryMonth"] as? String,
            let expiryYear = args["expiryYear"] as? String,
            let cardholderFirstName = args["cardholderFirstName"] as? String,
            let cardholderLastName = args["cardholderLastName"] as? String,
            let cardholderEmail = args["cardholderEmail"] as? String,
            let cardholderPhoneNumber = args["cardholderPhoneNumber"] as? String,
            let paymentSessionId = args["paymentSessionId"] as? String else {
        result(FlutterError(code: "INVALID_ARGUMENTS", message: "Missing required parameters", details: nil))
        return
      }
      
      let cvn = args["cvn"] as? String
      let confirmSave = args["confirmSave"] as? Bool ?? false
      
      // Get fingerprint if SDK is available
      let deviceFingerprint = getFingerprint(eventName: "collect_card_data")
      
      // Create request payload
      var requestPayload: [String: Any] = [
        "card_number": cardNumber,
        "expiry_month": expiryMonth,
        "expiry_year": expiryYear,
        "cardholder_first_name": cardholderFirstName,
        "cardholder_last_name": cardholderLastName,
        "cardholder_email": cardholderEmail,
        "cardholder_phone_number": cardholderPhoneNumber,
        "payment_session_id": paymentSessionId,
        "confirm_save": confirmSave,
        "device": ["fingerprint": deviceFingerprint]
      ]
      
      if let cvn = cvn {
        requestPayload["cvn"] = cvn
      }
      
      // Make API call using Flutter's platform channel to invoke Dart code
      makeApiRequest(method: "paymentWithSession", payload: requestPayload, apiKey: apiKey, result: result)
    case "collectCvn":
      guard let apiKey = self.apiKey else {
        result(FlutterError(code: "NOT_INITIALIZED", message: "SDK not initialized. Call initialize() first", details: nil))
        return
      }
      
      guard let args = call.arguments as? [String: Any],
            let cvn = args["cvn"] as? String,
            let paymentSessionId = args["paymentSessionId"] as? String else {
        result(FlutterError(code: "INVALID_ARGUMENTS", message: "Missing required parameters", details: nil))
        return
      }
      
      // Get fingerprint if SDK is available
      let deviceFingerprint = getFingerprint(eventName: "collect_cvn")
      
      // Create request payload
      let requestPayload: [String: Any] = [
        "cvn": cvn,
        "payment_session_id": paymentSessionId,
        "device": ["fingerprint": deviceFingerprint]
      ]
      
      // Make API call using Flutter's platform channel to invoke Dart code
      makeApiRequest(method: "paymentWithSession", payload: requestPayload, apiKey: apiKey, result: result)
    default:
      result(FlutterMethodNotImplemented)
    }
  }
  
  private func getFingerprint(eventName: String) -> String {
    #if canImport(XenditFingerprintSDK)
    if let sdk = self.fingerprintSDK {
      // Get session ID
      let sessionId = sdk.getSessionId()
      
      // Trigger scan
      sdk.scan(
        event_name: eventName,
        event_id: sessionId,
        completion: { response, errorMsg in
          if let errorMsg = errorMsg {
            NSLog("Scan failed for event: \(eventName), error: \(errorMsg)")
          }
        }
      )
      
      return sessionId
    }
    #endif
    
    // If SDK is not available or failed to initialize, return empty string
    // rather than a fake fingerprint
    return ""
  }
  
  private func makeApiRequest(method: String, payload: [String: Any], apiKey: String, result: @escaping FlutterResult) {
    guard let channel = self.channel else {
      result(FlutterError(code: "CHANNEL_ERROR", message: "Method channel not initialized", details: nil))
      return
    }
    
    // Convert payload to JSON string
    guard let payloadData = try? JSONSerialization.data(withJSONObject: payload),
          let payloadString = String(data: payloadData, encoding: .utf8) else {
      result(FlutterError(code: "JSON_ERROR", message: "Failed to serialize payload", details: nil))
      return
    }
    
    // Prepare arguments as a dictionary with all values being NSObject types
    let arguments: [String: Any] = [
      "method": method,
      "payload": payloadString,
      "apiKey": apiKey
    ]
    
    // Call back to Dart to make the API request
    channel.invokeMethod("makeApiRequest", arguments: arguments) { (response) in
      if let error = response as? FlutterError {
        result(error)
        return
      }
      
      if response == nil {
        result(nil)
        return
      }
      
      // Try to cast the response to a dictionary
      if let responseDict = response as? [String: Any] {
        result(responseDict)
        return
      }
      
      // If we get here, the response is in an unexpected format
      result(FlutterError(code: "INVALID_RESPONSE", message: "Invalid response format", details: nil))
    }
  }
}
