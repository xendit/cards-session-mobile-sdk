import 'package:flutter/services.dart';

class CardsResponse {
  final String message;
  final String? errorCode;

  CardsResponse({required this.message, this.errorCode});

  @override
  String toString() => message;
}

class XenditCardsPlugin {
  static const MethodChannel _channel = MethodChannel('xendit_cards_plugin');

  final String apiKey;

  XenditCardsPlugin({this.apiKey = ''});

  Future<CardsResponse> collectCardData({
    required String cardNumber,
    required String expiryMonth,
    required String expiryYear,
    String? cvn,
    required String firstName,
    required String lastName,
    required String email,
    required String phoneNumber,
    required String paymentSessionId,
  }) async {
    try {
      final result = await _channel.invokeMethod('collectCardData', {
        'cardNumber': cardNumber,
        'expiryMonth': expiryMonth,
        'expiryYear': expiryYear,
        'cvn': cvn,
        'cardholderFirstName': firstName,
        'cardholderLastName': lastName,
        'cardholderEmail': email,
        'cardholderPhoneNumber': phoneNumber,
        'paymentSessionId': paymentSessionId,
        'apiKey': apiKey,
      });

      return CardsResponse(message: result.toString());
    } on PlatformException catch (e) {
      return CardsResponse(
        message: e.message ?? 'Unknown error occurred',
        errorCode: e.code,
      );
    }
  }

  Future<CardsResponse> collectCvn({
    required String cvn,
    required String paymentSessionId,
  }) async {
    try {
      final result = await _channel.invokeMethod('collectCvn', {
        'cvn': cvn,
        'paymentSessionId': paymentSessionId,
        'apiKey': apiKey,
      });

      return CardsResponse(message: result.toString());
    } on PlatformException catch (e) {
      return CardsResponse(
        message: e.message ?? 'Unknown error occurred',
        errorCode: e.code,
      );
    }
  }
}
