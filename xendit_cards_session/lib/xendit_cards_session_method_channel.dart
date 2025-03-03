import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'xendit_cards_session_platform_interface.dart';

/// An implementation of [XenditCardsSessionPlatform] that uses method channels.
class MethodChannelXenditCardsSession extends XenditCardsSessionPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('xendit_cards_session');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
