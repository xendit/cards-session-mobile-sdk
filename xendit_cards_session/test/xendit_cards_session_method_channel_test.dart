import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:xendit_cards_session/xendit_cards_session_method_channel.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  MethodChannelXenditCardsSession platform = MethodChannelXenditCardsSession();
  const MethodChannel channel = MethodChannel('xendit_cards_session');

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      channel,
      (MethodCall methodCall) async {
        return '42';
      },
    );
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(channel, null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
