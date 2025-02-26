import 'package:flutter_test/flutter_test.dart';
import 'package:xendit_cards_session/xendit_cards_session.dart';
import 'package:xendit_cards_session/xendit_cards_session_platform_interface.dart';
import 'package:xendit_cards_session/xendit_cards_session_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockXenditCardsSessionPlatform
    with MockPlatformInterfaceMixin
    implements XenditCardsSessionPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final XenditCardsSessionPlatform initialPlatform = XenditCardsSessionPlatform.instance;

  test('$MethodChannelXenditCardsSession is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelXenditCardsSession>());
  });

  test('getPlatformVersion', () async {
    XenditCardsSession xenditCardsSessionPlugin = XenditCardsSession();
    MockXenditCardsSessionPlatform fakePlatform = MockXenditCardsSessionPlatform();
    XenditCardsSessionPlatform.instance = fakePlatform;

    expect(await xenditCardsSessionPlugin.getPlatformVersion(), '42');
  });
}
