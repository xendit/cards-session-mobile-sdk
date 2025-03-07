import 'dart:async';
import 'dart:convert';
import 'package:flutter/services.dart';
import 'src/models/cards_session_dto.dart';
import 'src/network/cards_client.dart';
import 'src/network/cards_session_exception.dart';
import 'src/utils/auth_token_generator.dart';

// Export utility classes
export 'src/utils/credit_card_util.dart';
export 'src/utils/auth_token_generator.dart';

class CardResponse {
  final String? message;
  final String? paymentTokenId;
  final String? actionUrl;

  CardResponse({this.message, this.paymentTokenId, this.actionUrl});

  factory CardResponse.fromJson(Map<String, dynamic> json) {
    return CardResponse(
      message: json['message'],
      paymentTokenId: json['payment_token_id'],
      actionUrl: json['action_url'],
    );
  }
}

class CardException {
  final String? errorCode;
  final String? message;

  CardException({this.errorCode, this.message});

  factory CardException.fromJson(Map<String, dynamic> json) {
    return CardException(
      errorCode: json['error_code'],
      message: json['message'],
    );
  }
}

class CardSessionState {
  final bool isLoading;
  final CardResponse? cardResponse;
  final CardException? exception;

  CardSessionState({
    this.isLoading = false,
    this.cardResponse,
    this.exception,
  });
}

class XenditCardsSession {
  static const MethodChannel _channel = MethodChannel('xendit_cards_session');
  final _stateController = StreamController<CardSessionState>.broadcast();
  final _cardsClient = CardsClient();

  Stream<CardSessionState> get state => _stateController.stream;
  CardSessionState _currentState = CardSessionState();

  XenditCardsSession() {
    _channel.setMethodCallHandler(_handleMethodCall);
  }

  Future<String?> getPlatformVersion() async {
    final version = await _channel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  Future<void> initialize({required String apiKey}) async {
    try {
      _updateState(isLoading: true);
      await _channel.invokeMethod('initialize', {'apiKey': apiKey});
      _updateState(isLoading: false);
    } catch (e) {
      _handleError(e);
    }
  }

  Future<CardResponse> collectCardData({
    required String cardNumber,
    required String expiryMonth,
    required String expiryYear,
    String? cvn,
    required String cardholderFirstName,
    required String cardholderLastName,
    required String cardholderEmail,
    required String cardholderPhoneNumber,
    required String paymentSessionId,
    bool confirmSave = false,
  }) async {
    try {
      _updateState(isLoading: true);
      final result = await _channel.invokeMethod('collectCardData', {
        'cardNumber': cardNumber,
        'expiryMonth': expiryMonth,
        'expiryYear': expiryYear,
        'cvn': cvn,
        'cardholderFirstName': cardholderFirstName,
        'cardholderLastName': cardholderLastName,
        'cardholderEmail': cardholderEmail,
        'cardholderPhoneNumber': cardholderPhoneNumber,
        'paymentSessionId': paymentSessionId,
        'confirmSave': confirmSave,
      });
      
      final response = CardResponse.fromJson(Map<String, dynamic>.from(result));
      _updateState(isLoading: false, cardResponse: response);
      return response;
    } catch (e) {
      _handleError(e);
      rethrow;
    }
  }

  Future<CardResponse> collectCvn({
    required String cvn,
    required String paymentSessionId,
  }) async {
    try {
      _updateState(isLoading: true);
      final result = await _channel.invokeMethod('collectCvn', {
        'cvn': cvn,
        'paymentSessionId': paymentSessionId,
      });
      
      final response = CardResponse.fromJson(Map<String, dynamic>.from(result));
      _updateState(isLoading: false, cardResponse: response);
      return response;
    } catch (e) {
      _handleError(e);
      rethrow;
    }
  }

  void _updateState({
    bool? isLoading,
    CardResponse? cardResponse,
    CardException? exception,
  }) {
    _currentState = CardSessionState(
      isLoading: isLoading ?? _currentState.isLoading,
      cardResponse: cardResponse ?? _currentState.cardResponse,
      exception: exception ?? _currentState.exception,
    );
    _stateController.add(_currentState);
  }

  void _handleError(dynamic error) {
    if (error is PlatformException) {
      final exception = CardException(
        errorCode: error.code,
        message: error.message,
      );
      _updateState(isLoading: false, exception: exception);
    } else {
      final exception = CardException(
        errorCode: 'unknown_error',
        message: error.toString(),
      );
      _updateState(isLoading: false, exception: exception);
    }
  }

  Future<dynamic> _handleMethodCall(MethodCall call) async {
    switch (call.method) {
      case 'onStateChanged':
        final Map<String, dynamic> arguments = Map<String, dynamic>.from(call.arguments);
        if (arguments.containsKey('isLoading')) {
          _updateState(isLoading: arguments['isLoading']);
        }
        if (arguments.containsKey('response')) {
          _updateState(
            cardResponse: CardResponse.fromJson(
              Map<String, dynamic>.from(arguments['response']),
            ),
          );
        }
        if (arguments.containsKey('error')) {
          _updateState(
            exception: CardException.fromJson(
              Map<String, dynamic>.from(arguments['error']),
            ),
          );
        }
        break;
      case 'makeApiRequest':
        // Handle API requests from native code
        final Map<String, dynamic> arguments = Map<String, dynamic>.from(call.arguments);
        final String payload = arguments['payload'];
        final String? apiKey = arguments['apiKey'];
        
        if (apiKey == null || apiKey.isEmpty) {
          return {'error': 'API key is not set'};
        }
        
        try {
          // Parse the payload from JSON
          final Map<String, dynamic> payloadMap = jsonDecode(payload);
          
          // Create the request DTO
          final request = _createRequestDto(payloadMap);
          
          // Generate auth token from API key
          final authToken = AuthTokenGenerator.generateAuthToken(apiKey);
          
          // Make the API call
          final response = await _cardsClient.paymentWithSession(request, authToken);
          
          // Convert the response to a map
          return {
            'message': response.message,
            'payment_token_id': response.paymentTokenId,
            'action_url': response.actionUrl,
          };
        } catch (e) {
          if (e is CardsSessionException) {
            return {
              'error_code': e.errorCode.toString(),
              'message': e.message,
            };
          } else {
            return {
              'error_code': 'unknown_error',
              'message': e.toString(),
            };
          }
        }
      default:
        break;
    }
  }
  
  CardsRequestDto _createRequestDto(Map<String, dynamic> payload) {
    // Extract device fingerprint
    final deviceData = payload['device'] as Map<String, dynamic>;
    final deviceFingerprint = DeviceFingerprint(
      fingerprint: deviceData['fingerprint'] as String,
    );
    
    // Create the request DTO
    return CardsRequestDto(
      cardNumber: payload['card_number'] as String?,
      expiryMonth: payload['expiry_month'] as String?,
      expiryYear: payload['expiry_year'] as String?,
      cvn: payload['cvn'] as String?,
      cardholderFirstName: payload['cardholder_first_name'] as String?,
      cardholderLastName: payload['cardholder_last_name'] as String?,
      cardholderEmail: payload['cardholder_email'] as String?,
      cardholderPhoneNumber: payload['cardholder_phone_number'] as String?,
      confirmSave: payload['confirm_save'] as bool?,
      paymentSessionId: payload['payment_session_id'] as String,
      device: deviceFingerprint,
    );
  }

  void dispose() {
    _stateController.close();
  }
}
