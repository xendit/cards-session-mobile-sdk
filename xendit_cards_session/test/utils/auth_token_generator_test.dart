import 'package:flutter_test/flutter_test.dart';
import 'package:xendit_cards_session/src/utils/auth_token_generator.dart';
import 'dart:convert';

void main() {
  group('AuthTokenGenerator', () {
    test('should generate correct auth token', () {
      const apiKey = 'test_api_key_123';
      final expectedToken = base64Encode(utf8.encode('$apiKey:'));
      
      final result = AuthTokenGenerator.generateAuthToken(apiKey);
      
      expect(result, expectedToken);
    });

    test('should include colon in encoded credentials', () {
      const apiKey = 'test_api_key_123';
      final result = AuthTokenGenerator.generateAuthToken(apiKey);
      
      // Decode the token to verify it contains the colon
      final decoded = utf8.decode(base64Decode(result));
      expect(decoded, '$apiKey:');
    });
  });
} 