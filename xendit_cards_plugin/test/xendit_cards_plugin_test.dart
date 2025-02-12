import 'package:flutter_test/flutter_test.dart';
import 'package:xendit_cards_plugin/xendit_cards_plugin_platform_interface.dart';
import 'package:xendit_cards_plugin/xendit_cards_plugin_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockXenditCardsPluginPlatform
    with MockPlatformInterfaceMixin
    implements XenditCardsPluginPlatform {
  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final XenditCardsPluginPlatform initialPlatform =
      XenditCardsPluginPlatform.instance;

  test('$MethodChannelXenditCardsPlugin is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelXenditCardsPlugin>());
  });
}
