import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'xendit_cards_session_method_channel.dart';

abstract class XenditCardsSessionPlatform extends PlatformInterface {
  /// Constructs a XenditCardsSessionPlatform.
  XenditCardsSessionPlatform() : super(token: _token);

  static final Object _token = Object();

  static XenditCardsSessionPlatform _instance = MethodChannelXenditCardsSession();

  /// The default instance of [XenditCardsSessionPlatform] to use.
  ///
  /// Defaults to [MethodChannelXenditCardsSession].
  static XenditCardsSessionPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [XenditCardsSessionPlatform] when
  /// they register themselves.
  static set instance(XenditCardsSessionPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
