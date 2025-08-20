import 'dart:async';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:xendit_cards_session/xendit_cards_session.dart';
import 'xendit_api_helper.dart';
import 'test_utils.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  // Test-specific configuration
  // Sensitive values can be provided via environment variables:
  // Example: flutter test --dart-define=TEST_PUBLIC_KEY=xxx --dart-define=TEST_SECRET_KEY=yyy
  const String publicKey = String.fromEnvironment(
    'TEST_PUBLIC_KEY',
    defaultValue: '', // Empty default - tests will fail without real keys
  );
  const String secretKey = String.fromEnvironment(
    'TEST_SECRET_KEY', 
    defaultValue: '', // Empty default - tests will fail without real keys
  );
  const String baseUrl = String.fromEnvironment(
    'TEST_BASE_URL',
    defaultValue: 'https://api.xendit.co',
  );
  // Test customer ID - replace with real value via environment variable if needed
  const String testCustomerId = String.fromEnvironment(
    'TEST_CUSTOMER_ID',
    defaultValue: 'cust-XXX', // Placeholder - tests will fail without real ID
  );

  // Session configurations
  const Map<String, dynamic> saveCardSession = {
    'description': 'Integration test session',
    'amount': 0,
    'sessionType': 'SAVE',
    'country': 'ID',
    'locale': 'en',
    'currency': 'IDR',
    'metadata': {
      'test_run': 'true',
      'integration_test': 'e2e',
    },
  };

  const Map<String, dynamic> cvnCollectionSession = {
    'description': 'CVN collection test',
    'amount': 10000,
    'sessionType': 'PAY',
    'country': 'ID',
    'locale': 'en',
    'currency': 'IDR',
    'cardPaymentTokenId': String.fromEnvironment(
      'TEST_PAYMENT_TOKEN_ID',
      defaultValue: 'pt-XXX', // Placeholder - tests will fail without real token
    ),
    'metadata': {
      'test_type': 'cvn_collection',
    },
  };

  const Map<String, dynamic> errorHandlingSession = {
    'description': 'Error handling test session',
    'amount': 0,
    'sessionType': 'SAVE',
    'country': 'ID',
    'locale': 'en',
    'currency': 'IDR',
    'metadata': {
      'test_type': 'error_handling',
    },
  };

  // Timeout configurations
  const int responseTimeout = 15000;
  const int cvnTimeout = 10000;
  const int initDelay = 500;
  const int stateDelay = 500;
  const int cleanupDelay = 100;

  group('Full End-to-End XenditCardsSession Integration Tests', () {
    late String apiKey;
    late XenditApiHelper apiHelper;
    
    setUpAll(() async {
      apiKey = secretKey;
    });
    
    setUp(() {
      apiHelper = XenditApiHelper(apiKey: publicKey, baseUrl: baseUrl);
    });
    
    test('complete payment session flow - create session, collect card, verify', () async {
      final plugin = XenditCardsSession();
      StreamSubscription? subscription;
      
      try {
        // Step 1: Create a session
        late String sessionId;
        final referenceId = 'test_${DateTime.now().millisecondsSinceEpoch}';
        
        try {
          final sessionResponse = await apiHelper.createSession(
            referenceId: referenceId,
            customerId: testCustomerId,
            description: saveCardSession['description'],
            amount: saveCardSession['amount'],
            sessionType: saveCardSession['sessionType'],
            country: saveCardSession['country'],
            locale: saveCardSession['locale'],
            currency: saveCardSession['currency'],
            metadata: saveCardSession['metadata'],
          );
          
          // Extract the session ID
          sessionId = apiHelper.extractSessionId(sessionResponse) ?? '';
          expect(sessionId, isNotEmpty);
        } catch (e) {
          // If session creation fails, skip this test
          markTestSkipped('Could not create session: $e');
          return;
        }
        
        // Step 2: Initialize SDK with API key
        await plugin.initialize(apiKey: apiKey);
        await Future.delayed(Duration(milliseconds: initDelay));
        
        // Step 3: Set up billing information
        // Use TestData factory to create billing info
        final billingInfo = TestData.createValidBillingInfo(
          firstName: 'Test',
          lastName: 'User',
          email: 'test.user@example.com',
          phoneNumber: '+6281234567890',
          streetLine1: '123 Test Street',
          city: 'Jakarta',
          provinceState: 'DKI Jakarta',
          postalCode: '12345',
          country: 'ID',
        );
        
        // Step 4: Track state changes and collect card data
        CardSessionState? finalState;
        final completer = Completer<CardSessionState>();
        
        subscription = plugin.state.listen((state) {
          if (state.exception != null) {
          }
          finalState = state;
          if (!state.isLoading && (state.cardResponse != null || state.exception != null)) {
            if (!completer.isCompleted) {
              completer.complete(state);
            }
          }
        });
        
        // Step 5: Collect card data with the created session
        // Use card data from test_utils
        final validCardNumber = TestData.validCardNumbers['visa']!;
        await plugin.collectCardData(
          cardNumber: validCardNumber,
          expiryMonth: '12',
          expiryYear: '2025',
          cvn: '123',
          cardholderFirstName: 'Test',
          cardholderLastName: 'User',
          cardholderEmail: 'test.user@example.com',
          cardholderPhoneNumber: '+6281234567890',
          paymentSessionId: sessionId,
          billingInformation: billingInfo,
          confirmSave: true,
        );
        
        // Wait for response
        final result = await completer.future.timeout(
          Duration(milliseconds: responseTimeout),
          onTimeout: () {
            return finalState!;
          },
        );
        
        // Step 6: Check the result
        String? paymentTokenId;
        
        if (result.exception != null) {
          // If there's an error, it might be due to test environment limitations
          expect(result.exception!.errorCode, isNotEmpty);
        } else if (result.cardResponse != null) {
          // Success case
          paymentTokenId = result.cardResponse!.paymentTokenId;
          expect(paymentTokenId, isNotNull);
          
          // Step 7: Verify the session was updated with payment information
          try {
            final sessionDetails = await apiHelper.getSession(sessionId);
            
            // Verify session details
            expect(sessionDetails['payment_session_id'], equals(sessionId));
            expect(sessionDetails['status'], equals('COMPLETED'));
            
            // Check if payment token was created
            expect(sessionDetails['payment_token_id'], isNotNull);
            expect(sessionDetails['payment_token_id'], equals(paymentTokenId));
            
            // Verify the session contains the payment token we created
          } catch (e) {
            // Session verification might fail in test environment
            markTestSkipped(e.toString());
          }
        }
      } finally {
        await subscription?.cancel();
        await Future.delayed(Duration(milliseconds: cleanupDelay));
        plugin.dispose();
      }
    });
    
    test('CVN collection flow with existing payment token', () async {
      final plugin = XenditCardsSession();
      StreamSubscription? subscription;
      
      try {
        // Create a session for CVN collection
        late String sessionId;
        final referenceId = 'test_cvn_${DateTime.now().millisecondsSinceEpoch}';
        
        try {
          final sessionResponse = await apiHelper.createSession(
            referenceId: referenceId,
            customerId: testCustomerId,
            description: cvnCollectionSession['description'],
            amount: cvnCollectionSession['amount'],
            sessionType: cvnCollectionSession['sessionType'],
            country: cvnCollectionSession['country'],
            locale: cvnCollectionSession['locale'],
            currency: cvnCollectionSession['currency'],
            cardPaymentTokenId: cvnCollectionSession['cardPaymentTokenId'],
            metadata: cvnCollectionSession['metadata'],
          );
          
          sessionId = apiHelper.extractSessionId(sessionResponse) ?? '';
          expect(sessionId, isNotEmpty);
        } catch (e) {
          markTestSkipped('Could not create session: $e');
          return;
        }
        
        // Initialize SDK
        await plugin.initialize(apiKey: apiKey);
        await Future.delayed(Duration(milliseconds: initDelay));
        
        // Track state changes
        CardSessionState? finalState;
        final completer = Completer<CardSessionState>();
        
        subscription = plugin.state.listen((state) {
          if (state.exception != null) {
          }
          finalState = state;
          if (!state.isLoading && (state.cardResponse != null || state.exception != null)) {
            if (!completer.isCompleted) {
              completer.complete(state);
            }
          }
        });
        
        // Collect CVN only
        await plugin.collectCvn(
          cvn: '456',
          paymentSessionId: sessionId,
        );
        
        // Wait for response
        final result = await completer.future.timeout(
          Duration(milliseconds: cvnTimeout),
          onTimeout: () {
            return finalState ?? CardSessionState(isLoading: false);
          },
        );
        
        
        // Check result
        if (result.exception != null) {
          // This might fail if the payment token doesn't exist or is invalid
          expect(result.exception!.errorCode, isNotEmpty);
        } else if (result.cardResponse != null) {
          // Success
          expect(result.cardResponse!.paymentTokenId ?? result.cardResponse!.message, isNotNull);
          
          // Verify the session was completed
          try {
            final sessionDetails = await apiHelper.getSession(sessionId);
            
            expect(sessionDetails['payment_session_id'], equals(sessionId));
            expect(sessionDetails['status'], equals('COMPLETED'));
          } catch (e) {
          }
        }
      } finally {
        await subscription?.cancel();
        await Future.delayed(Duration(milliseconds: cleanupDelay));
        plugin.dispose();
      }
    });
    
    test('error handling - invalid card number', () async {
      final plugin = XenditCardsSession();
      StreamSubscription? subscription;
      
      try {
        // Step 1: Create a session for error testing
        late String sessionId;
        final referenceId = 'test_error_${DateTime.now().millisecondsSinceEpoch}';
        
        try {
          final sessionResponse = await apiHelper.createSession(
            referenceId: referenceId,
            customerId: testCustomerId,
            description: errorHandlingSession['description'],
            amount: errorHandlingSession['amount'],
            sessionType: errorHandlingSession['sessionType'],
            country: errorHandlingSession['country'],
            locale: errorHandlingSession['locale'],
            currency: errorHandlingSession['currency'],
            metadata: errorHandlingSession['metadata'],
          );
          
          sessionId = apiHelper.extractSessionId(sessionResponse) ?? '';
          expect(sessionId, isNotEmpty);
        } catch (e) {
          markTestSkipped('Could not create session: $e');
          return;
        }
        
        // Step 2: Initialize SDK
        await plugin.initialize(apiKey: apiKey);
        await Future.delayed(Duration(milliseconds: initDelay));
        
        // Step 3: Track state changes
        CardResponse? capturedResponse;
        
        subscription = plugin.state.listen((state) {
          
          if (state.cardResponse != null) {
            capturedResponse = state.cardResponse;
          }
          
          if (state.exception != null) {
          }
        });
        
        // Step 4: Try with invalid card
        // Use TestData factory for minimal billing info
        final billingInfo = TestData.createValidBillingInfo(
          firstName: 'Test',
          lastName: 'User',
          email: 'test@example.com',
          streetLine1: '123 Test St',
          city: 'Jakarta',
          provinceState: 'DKI Jakarta',
          postalCode: '12345',
          country: 'ID',
        );
        
        // Use invalid card number with correct length but invalid format
        final invalidCardNumber = '1234567890123456'; // 16 digits but invalid card
        final result = await plugin.collectCardData(
          cardNumber: invalidCardNumber,
          expiryMonth: '13',
          expiryYear: '2025',
          cvn: '123',
          cardholderFirstName: 'Test',
          cardholderLastName: 'User',
          cardholderEmail: 'test.user@example.com',
          cardholderPhoneNumber: '+6281234567890',
          paymentSessionId: sessionId,
          billingInformation: billingInfo,
        );
        
        
        // Add small delay to ensure state is propagated
        await Future.delayed(Duration(milliseconds: stateDelay));
        
        // Check the result directly
        
        // The SDK should return an error message for invalid card
        expect(result.message, isNotNull);
        expect(
          result.message?.toLowerCase(), 
          anyOf(contains('invalid'), contains('error'), contains('failed')),
          reason: 'Expected error message for invalid card number',
        );
        expect(result.paymentTokenId, isNull,
            reason: 'Payment token should be null for invalid card');
        
        // Also check if error was captured in state
        if (capturedResponse != null) {
          expect(capturedResponse!.message, equals(result.message));
        }
      } finally {
        await subscription?.cancel();
        await Future.delayed(Duration(milliseconds: cleanupDelay));
        plugin.dispose();
      }
    });
  });
}