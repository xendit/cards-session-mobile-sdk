import 'package:flutter/foundation.dart';

/// Network-related constants
class NetworkConstants {
  /// Base URL for API requests
  /// Returns staging URL in debug mode, production URL otherwise
  static String get baseUrl {
    return kDebugMode 
        ? 'https://api.stg.tidnex.dev/v3' 
        : 'https://api.xendit.co/v3';
  }
} 