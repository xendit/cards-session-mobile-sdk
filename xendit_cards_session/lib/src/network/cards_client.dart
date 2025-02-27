import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import '../models/cards_session_dto.dart';
import 'cards_session_exception.dart';
import 'network_constants.dart';

/// Client for making API calls to the cards service
class CardsClient {
  final http.Client _httpClient;
  
  CardsClient({http.Client? httpClient}) : _httpClient = httpClient ?? http.Client();

  /// Makes a payment with session request
  Future<CardsResponseDto> paymentWithSession(
    CardsRequestDto request,
    String authToken,
  ) async {
    try {
      final jsonBody = jsonEncode(request.toJson());
      debugPrint('Making payment session request with body: $jsonBody');
      debugPrint('Auth token: $authToken');
      
      final uri = Uri.parse('${NetworkConstants.baseUrl}/payment_with_session');
      debugPrint('Request URL: $uri');

      final response = await _httpClient.post(
        uri,
        headers: {
          'Authorization': 'Basic $authToken',
          'Content-Type': 'application/json',
        },
        body: jsonBody,
      );

      debugPrint('Response status: ${response.statusCode}');
      debugPrint('Response body: ${response.body}');
      
      if (response.statusCode >= 200 && response.statusCode < 300) {
        final responseBody = jsonDecode(response.body) as Map<String, dynamic>;
        debugPrint('Payment session request successful. Response: $responseBody');
        return CardsResponseDto.fromJson(responseBody);
      } else {
        final errorBody = jsonDecode(response.body) as Map<String, dynamic>;
        debugPrint('Payment session request failed. Error: $errorBody');
        
        final errorCode = _mapErrorCode(errorBody['error_code'] as String?);
        throw CardsSessionException(
          errorCode: errorCode,
          message: errorBody['message'] as String? ?? 'Unknown error',
        );
      }
    } on CardsSessionException {
      rethrow;
    } catch (e) {
      debugPrint('Unknown error occurred: $e');
      throw CardsSessionException(
        errorCode: CardsSessionError.unknownError,
        message: e.toString(),
      );
    }
  }

  CardsSessionError _mapErrorCode(String? errorCode) {
    switch (errorCode) {
      case 'SERVICE_UNAVAILABLE':
        return CardsSessionError.serviceUnavailable;
      case 'INVALID_OAUTH_TOKEN':
        return CardsSessionError.invalidOauthToken;
      case 'INVALID_TOKEN_ERROR':
        return CardsSessionError.invalidTokenError;
      case 'SERVER_ERROR':
        return CardsSessionError.serverError;
      case 'API_VALIDATION_ERROR':
        return CardsSessionError.apiValidationError;
      default:
        return CardsSessionError.unknownError;
    }
  }
} 