import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'xendit_cards_plugin_method_channel.dart';

abstract class XenditCardsPluginPlatform extends PlatformInterface {
  /// Constructs a XenditCardsPluginPlatform.
  XenditCardsPluginPlatform() : super(token: _token);

  static final Object _token = Object();

  static XenditCardsPluginPlatform _instance = MethodChannelXenditCardsPlugin();

  /// The default instance of [XenditCardsPluginPlatform] to use.
  ///
  /// Defaults to [MethodChannelXenditCardsPlugin].
  static XenditCardsPluginPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [XenditCardsPluginPlatform] when
  /// they register themselves.
  static set instance(XenditCardsPluginPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
