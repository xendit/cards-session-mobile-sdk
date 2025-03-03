import 'dart:convert';

/// Utility class for generating authentication tokens
class AuthTokenGenerator {
  /// Generates an auth token from an API key using Basic Auth format
  /// Format: Base64(apiKey:)
  /// 
  /// @param apiKey The API key to encode
  /// @return The encoded auth token
  static String generateAuthToken(String apiKey) {
    // Format as "apiKey:" (note the colon at the end with no password)
    final credentials = "$apiKey:";
    return base64Encode(utf8.encode(credentials));
  }
} 