import Flutter
import UIKit
import cardsSdk

public class XenditCardsPlugin: NSObject, FlutterPlugin {
  private let cardSessions: CardSessions
  private var apiKey: String = ""

  init(apiKey: String) {
    self.apiKey = apiKey
    self.cardSessions = CardSessionsFactory().create(apiKey: apiKey)
    super.init()
  }

  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "xendit_cards_plugin", binaryMessenger: registrar.messenger())
    let instance = XenditCardsPlugin(apiKey: "xnd_public_development_YOUR_KEY_HERE")
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
    case "collectCardData":
      guard let args = call.arguments as? [String: Any] else {
        result(FlutterError(code: "INVALID_ARGUMENTS", message: "Arguments are required", details: nil))
        return
      }
      // Implementation will be added here
      result(FlutterMethodNotImplemented)
      
    case "collectCvn":
      guard let args = call.arguments as? [String: Any] else {
        result(FlutterError(code: "INVALID_ARGUMENTS", message: "Arguments are required", details: nil))
        return
      }
      // Implementation will be added here
      result(FlutterMethodNotImplemented)
      
    default:
      result(FlutterMethodNotImplemented)
    }
  }
}
