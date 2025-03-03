/// Error codes for card session operations
enum CardsSessionError {
  serviceUnavailable,
  invalidOauthToken,
  invalidTokenError,
  serverError,
  apiValidationError,
  unknownError,
}

/// Exception thrown during card session operations
class CardsSessionException implements Exception {
  final CardsSessionError errorCode;
  final String message;

  CardsSessionException({
    required this.errorCode,
    required this.message,
  });

  @override
  String toString() => 'CardsSessionException: $message (code: $errorCode)';
} 