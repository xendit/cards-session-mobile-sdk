import 'dart:async';
import 'package:xendit_cards_session/xendit_cards_session.dart';

class TestData {
  static const String testApiKey = 'test_development_key_123456789';
  static const String testSessionId = 'test_session_id_123456789';
  
  static const Map<String, String> validCardNumbers = {
    'visa': '4111111111111111',
    'mastercard': '5500000000000004',
    'amex': '340000000000009',
    'discover': '6011000000000004',
    'jcb': '3528000000000007',
  };
  
  static const Map<String, String> invalidCardNumbers = {
    'too_short': '411111111111',
    'too_long': '41111111111111111',
    'letters': '411111111111abcd',
    'special_chars': '4111-1111-1111-1111',
  };
  
  static BillingInformationDto createValidBillingInfo({
    String firstName = 'John',
    String lastName = 'Doe',
    String email = 'john.doe@example.com',
    String? phoneNumber = '+1234567890',
    String streetLine1 = '123 Main Street',
    String? streetLine2,
    String city = 'New York',
    String provinceState = 'NY',
    String postalCode = '10001',
    String country = 'US',
  }) {
    return BillingInformationDto(
      firstName: firstName,
      lastName: lastName,
      email: email,
      phoneNumber: phoneNumber,
      streetLine1: streetLine1,
      streetLine2: streetLine2,
      city: city,
      provinceState: provinceState,
      postalCode: postalCode,
      country: country,
    );
  }
  
  static Map<String, dynamic> createValidCardData({
    String cardNumber = '4111111111111111',
    String expiryMonth = '12',
    String expiryYear = '2025',
    String? cvn = '123',
    String firstName = 'John',
    String lastName = 'Doe',
    String email = 'john.doe@example.com',
    String phoneNumber = '+1234567890',
    String sessionId = testSessionId,
    bool confirmSave = false,
    BillingInformationDto? billingInfo,
  }) {
    return {
      'cardNumber': cardNumber,
      'expiryMonth': expiryMonth,
      'expiryYear': expiryYear,
      'cvn': cvn,
      'cardholderFirstName': firstName,
      'cardholderLastName': lastName,
      'cardholderEmail': email,
      'cardholderPhoneNumber': phoneNumber,
      'paymentSessionId': sessionId,
      'confirmSave': confirmSave,
      'billingInformation': billingInfo ?? createValidBillingInfo(),
    };
  }
  
  static List<Map<String, dynamic>> getInvalidCardDataScenarios() {
    return [
      {
        'description': 'Invalid card number',
        'data': createValidCardData(cardNumber: '1234567890123456'),
        'expectedError': 'invalid_card_number',
      },
      {
        'description': 'Expired card',
        'data': createValidCardData(expiryYear: '2020'),
        'expectedError': 'card_expired',
      },
      {
        'description': 'Invalid month',
        'data': createValidCardData(expiryMonth: '13'),
        'expectedError': 'invalid_expiry_date',
      },
      {
        'description': 'Invalid email',
        'data': createValidCardData(email: 'not-an-email'),
        'expectedError': 'invalid_email',
      },
      {
        'description': 'Empty required fields',
        'data': createValidCardData(firstName: '', lastName: ''),
        'expectedError': 'missing_required_fields',
      },
    ];
  }
  
  static List<BillingInformationDto> getInternationalBillingAddresses() {
    return [
      createValidBillingInfo(
        country: 'GB',
        postalCode: 'SW1A 1AA',
        provinceState: 'London',
        city: 'London',
        streetLine1: '10 Downing Street',
      ),
      createValidBillingInfo(
        country: 'JP',
        postalCode: '100-0001',
        provinceState: 'Tokyo',
        city: 'Chiyoda',
        streetLine1: '1-1-1 Chiyoda',
      ),
      createValidBillingInfo(
        country: 'AU',
        postalCode: '2000',
        provinceState: 'NSW',
        city: 'Sydney',
        streetLine1: '1 Martin Place',
      ),
      createValidBillingInfo(
        country: 'CA',
        postalCode: 'M5H 2N2',
        provinceState: 'ON',
        city: 'Toronto',
        streetLine1: '100 King Street West',
      ),
    ];
  }
}

class TestHelpers {
  static Future<void> waitForState(
    XenditCardsSession plugin, {
    required bool Function(CardSessionState) condition,
    Duration timeout = const Duration(seconds: 5),
  }) async {
    final completer = Completer<void>();
    final subscription = plugin.state.listen((state) {
      if (condition(state) && !completer.isCompleted) {
        completer.complete();
      }
    });
    
    try {
      await completer.future.timeout(timeout);
    } finally {
      await subscription.cancel();
    }
  }
  
  static Future<List<CardSessionState>> collectStates(
    XenditCardsSession plugin,
    Future<void> Function() action, {
    Duration collectDuration = const Duration(milliseconds: 500),
  }) async {
    final states = <CardSessionState>[];
    final subscription = plugin.state.listen(states.add);
    
    await action();
    await Future.delayed(collectDuration);
    
    await subscription.cancel();
    return states;
  }
  
  static Future<void> ensureInitialized(
    XenditCardsSession plugin, {
    String apiKey = TestData.testApiKey,
  }) async {
    await plugin.initialize(apiKey: apiKey);
    await waitForState(
      plugin,
      condition: (state) => !state.isLoading,
      timeout: const Duration(seconds: 2),
    );
  }
}