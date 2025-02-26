import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:xendit_cards_session/xendit_cards_session.dart';
import 'package:xendit_cards_session/xendit_cards_session_platform_interface.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockXenditCardsSessionPlatform
    with MockPlatformInterfaceMixin
    implements XenditCardsSessionPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  
  const MethodChannel channel = MethodChannel('xendit_cards_session');
  final List<MethodCall> log = <MethodCall>[];
  
  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      channel,
      (MethodCall methodCall) async {
        log.add(methodCall);
        switch (methodCall.method) {
          case 'getPlatformVersion':
            return 'Android 10';
          case 'initialize':
            return null;
          case 'collectCardData':
            return {
              'message': 'Status updated. Wait for callback',
              'payment_token_id': 'pt-test-12345',
              'action_url': 'https://dictionary.cambridge.org/dictionary/english/success'
            };
          case 'collectCvn':
            return {
              'message': 'CVN collected successfully',
              'payment_token_id': 'pt-test-12345'
            };
          default:
            return null;
        }
      },
    );
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(channel, null);
    log.clear();
  });

  test('getPlatformVersion', () async {
    final xenditCardsSession = XenditCardsSession();
    expect(await xenditCardsSession.getPlatformVersion(), 'Android 10');
  });

  test('initialize', () async {
    final xenditCardsSession = XenditCardsSession();
    await xenditCardsSession.initialize(apiKey: 'test_api_key');
    expect(
      log,
      <Matcher>[
        isMethodCall(
          'initialize',
          arguments: {'apiKey': 'test_api_key'},
        ),
      ],
    );
  });

  test('collectCardData', () async {
    final xenditCardsSession = XenditCardsSession();
    final response = await xenditCardsSession.collectCardData(
      cardNumber: '4242424242424242',
      expiryMonth: '12',
      expiryYear: '2025',
      cvn: '123',
      cardholderFirstName: 'John',
      cardholderLastName: 'Doe',
      cardholderEmail: 'john@example.com',
      cardholderPhoneNumber: '+1234567890',
      paymentSessionId: 'ps-test-12345',
      confirmSave: true,
    );
    
    expect(response.message, 'Status updated. Wait for callback');
    expect(response.paymentTokenId, 'pt-test-12345');
    expect(response.actionUrl, 'https://dictionary.cambridge.org/dictionary/english/success');
    
    expect(
      log,
      <Matcher>[
        isMethodCall(
          'collectCardData',
          arguments: {
            'cardNumber': '4242424242424242',
            'expiryMonth': '12',
            'expiryYear': '2025',
            'cvn': '123',
            'cardholderFirstName': 'John',
            'cardholderLastName': 'Doe',
            'cardholderEmail': 'john@example.com',
            'cardholderPhoneNumber': '+1234567890',
            'paymentSessionId': 'ps-test-12345',
            'confirmSave': true,
          },
        ),
      ],
    );
  });

  test('collectCvn', () async {
    final xenditCardsSession = XenditCardsSession();
    final response = await xenditCardsSession.collectCvn(
      cvn: '123',
      paymentSessionId: 'ps-test-12345',
    );
    
    expect(response.message, 'CVN collected successfully');
    expect(response.paymentTokenId, 'pt-test-12345');
    
    expect(
      log,
      <Matcher>[
        isMethodCall(
          'collectCvn',
          arguments: {
            'cvn': '123',
            'paymentSessionId': 'ps-test-12345',
          },
        ),
      ],
    );
  });
}
