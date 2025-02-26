import Flutter
import UIKit

public class XenditCardsSessionPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "xendit_cards_session", binaryMessenger: registrar.messenger())
    let instance = XenditCardsSessionPlugin()
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
      // Implementation for initializing the SDK
      result(nil)
    case "collectCardData":
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
      
      // Mock implementation - return a sample success response
      let mockResponse: [String: Any] = [
        "message": "Status updated. Wait for callback",
        "payment_token_id": "pt-mock-12345",
        "action_url": "https://redirect-gateway.example.com"
      ]
      result(mockResponse)
    case "collectCvn":
      guard let args = call.arguments as? [String: Any],
            let cvn = args["cvn"] as? String,
            let paymentSessionId = args["paymentSessionId"] as? String else {
        result(FlutterError(code: "INVALID_ARGUMENTS", message: "Missing required parameters", details: nil))
        return
      }
      
      // Mock implementation - return a sample success response
      let mockResponse: [String: Any] = [
        "message": "CVN collected successfully",
        "payment_token_id": "pt-mock-12345"
      ]
      result(mockResponse)
    default:
      result(FlutterMethodNotImplemented)
    }
  }
}
