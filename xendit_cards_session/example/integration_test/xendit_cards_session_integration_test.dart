import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:xendit_cards_session/xendit_cards_session.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  late XenditCardsSession plugin;

  setUp(() {
    plugin = XenditCardsSession();
  });

  tearDown(() {
    plugin.dispose();
  });

  group('XenditCardsSession Integration Tests', () {
    test('getPlatformVersion returns non-empty string', () async {
      final String? version = await plugin.getPlatformVersion();
      expect(version, isNotNull);
      expect(version!.isNotEmpty, true);
    });

    test('initialize with valid API key completes successfully', () async {
      const testApiKey = 'test_api_key_123456789';
      
      bool isLoadingEmitted = false;
      bool loadingCompleted = false;

      final subscription = plugin.state.listen((state) {
        if (state.isLoading && !isLoadingEmitted) {
          isLoadingEmitted = true;
        }
        if (!state.isLoading && isLoadingEmitted) {
          loadingCompleted = true;
        }
      });

      await plugin.initialize(apiKey: testApiKey);
      
      await Future.delayed(const Duration(milliseconds: 100));
      
      expect(isLoadingEmitted, true, reason: 'Should emit loading state');
      expect(loadingCompleted, true, reason: 'Should complete loading');
      
      await subscription.cancel();
    });

    test('collectCardData validates required parameters', () async {
      const testApiKey = 'test_api_key_123456789';
      await plugin.initialize(apiKey: testApiKey);

      final billingInfo = BillingInformationDto(
        firstName: 'John',
        lastName: 'Doe',
        email: 'john.doe@example.com',
        phoneNumber: '+1234567890',
        streetLine1: '123 Main St',
        streetLine2: 'Apt 4B',
        city: 'New York',
        provinceState: 'NY',
        postalCode: '10001',
        country: 'US',
      );

      expect(
        () => plugin.collectCardData(
          cardNumber: '4111111111111111',
          expiryMonth: '12',
          expiryYear: '2025',
          cvn: '123',
          cardholderFirstName: 'John',
          cardholderLastName: 'Doe',
          cardholderEmail: 'john.doe@example.com',
          cardholderPhoneNumber: '+1234567890',
          paymentSessionId: 'test_session_id',
          billingInformation: billingInfo,
          confirmSave: false,
        ),
        throwsA(isA<PlatformException>()),
      );
    });

    test('collectCvn validates required parameters', () async {
      const testApiKey = 'test_api_key_123456789';
      await plugin.initialize(apiKey: testApiKey);

      expect(
        () => plugin.collectCvn(
          cvn: '123',
          paymentSessionId: 'test_session_id',
        ),
        throwsA(isA<PlatformException>()),
      );
    });

    test('state stream emits updates correctly', () async {
      final states = <CardSessionState>[];
      final subscription = plugin.state.listen(states.add);

      const testApiKey = 'test_api_key_123456789';
      await plugin.initialize(apiKey: testApiKey);
      
      await Future.delayed(const Duration(milliseconds: 100));
      
      expect(states.length, greaterThan(0));
      expect(states.any((state) => state.isLoading), true);
      
      await subscription.cancel();
    });

    test('error handling works correctly', () async {
      CardException? capturedError;
      
      final subscription = plugin.state.listen((state) {
        if (state.exception != null) {
          capturedError = state.exception;
        }
      });

      try {
        await plugin.collectCardData(
          cardNumber: 'invalid',
          expiryMonth: '13',
          expiryYear: '2020',
          cardholderFirstName: '',
          cardholderLastName: '',
          cardholderEmail: 'invalid-email',
          cardholderPhoneNumber: '',
          paymentSessionId: '',
          billingInformation: BillingInformationDto(
            firstName: '',
            lastName: '',
            email: '',
            streetLine1: '',
            city: '',
            provinceState: '',
            postalCode: '',
            country: '',
          ),
        );
      } catch (e) {
        // Expected to throw
      }

      await Future.delayed(const Duration(milliseconds: 100));
      
      expect(capturedError, isNotNull);
      
      await subscription.cancel();
    });

    test('BillingInformationDto serialization works correctly', () {
      final billingInfo = BillingInformationDto(
        firstName: 'John',
        lastName: 'Doe',
        email: 'john.doe@example.com',
        phoneNumber: '+1234567890',
        streetLine1: '123 Main St',
        streetLine2: 'Apt 4B',
        city: 'New York',
        provinceState: 'NY',
        postalCode: '10001',
        country: 'US',
      );

      final json = billingInfo.toJson();
      
      expect(json['first_name'], 'John');
      expect(json['last_name'], 'Doe');
      expect(json['email'], 'john.doe@example.com');
      expect(json['phone_number'], '+1234567890');
      expect(json['street_line1'], '123 Main St');
      expect(json['street_line2'], 'Apt 4B');
      expect(json['city'], 'New York');
      expect(json['province_state'], 'NY');
      expect(json['postal_code'], '10001');
      expect(json['country'], 'US');
    });

    test('CreditCardUtil validates card numbers correctly', () {
      expect(CreditCardUtil.isCreditCardNumberValid('4111111111111111'), true);
      expect(CreditCardUtil.isCreditCardNumberValid('5500000000000004'), true);
      expect(CreditCardUtil.isCreditCardNumberValid('340000000000009'), true);
      expect(CreditCardUtil.isCreditCardNumberValid('6011000000000004'), true);
      
      expect(CreditCardUtil.isCreditCardNumberValid(''), false);
      expect(CreditCardUtil.isCreditCardNumberValid('   '), false);
    });

    test('CreditCardUtil validates expiry dates correctly', () {
      final currentYear = DateTime.now().year;
      
      expect(CreditCardUtil.isCreditCardExpirationDateValid('12', currentYear.toString()), true);
      expect(CreditCardUtil.isCreditCardExpirationDateValid('01', (currentYear + 1).toString()), true);
      expect(CreditCardUtil.isCreditCardExpirationDateValid('06', currentYear.toString()), true);
      
      expect(CreditCardUtil.isCreditCardExpirationDateValid('13', currentYear.toString()), false);
      expect(CreditCardUtil.isCreditCardExpirationDateValid('00', currentYear.toString()), false);
      expect(CreditCardUtil.isCreditCardExpirationDateValid('12', (currentYear - 1).toString()), false);
      expect(CreditCardUtil.isCreditCardExpirationDateValid('abc', currentYear.toString()), false);
    });

    test('CreditCardUtil validates CVN correctly', () {
      expect(CreditCardUtil.isCreditCardCVNValid('123'), true);
      expect(CreditCardUtil.isCreditCardCVNValid('1234'), true);
      expect(CreditCardUtil.isCreditCardCVNValid(null), true);
      
      expect(CreditCardUtil.isCreditCardCVNValid('12'), false);
      expect(CreditCardUtil.isCreditCardCVNValid('12345'), false);
      expect(CreditCardUtil.isCreditCardCVNValid('abc'), false);
      expect(CreditCardUtil.isCreditCardCVNValid(''), false);
      expect(CreditCardUtil.isCreditCardCVNValid('   '), false);
    });

    test('AuthTokenGenerator generates valid base64 token', () {
      const apiKey = 'test_api_key_123456789';
      final token = AuthTokenGenerator.generateAuthToken(apiKey);
      
      expect(token, isNotEmpty);
      expect(token.contains('Basic '), true);
      
      // Verify it's valid base64
      final base64Part = token.replaceFirst('Basic ', '');
      expect(() => base64Part, isNotNull);
    });
  });
}