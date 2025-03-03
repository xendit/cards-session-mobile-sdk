import 'package:meta/meta.dart';

/// Request model for card data collection
@immutable
class CardsRequestDto {
  final String? cardNumber;
  final String? expiryMonth;
  final String? expiryYear;
  final String? cardholderFirstName;
  final String? cardholderLastName;
  final String? cardholderEmail;
  final String? cardholderPhoneNumber;
  final String? cvn;
  final bool? confirmSave;
  final String paymentSessionId;
  final DeviceFingerprint device;

  const CardsRequestDto({
    this.cardNumber,
    this.expiryMonth,
    this.expiryYear,
    this.cardholderFirstName,
    this.cardholderLastName,
    this.cardholderEmail,
    this.cardholderPhoneNumber,
    this.cvn,
    this.confirmSave = false,
    required this.paymentSessionId,
    required this.device,
  });

  Map<String, dynamic> toJson() {
    return {
      if (cardNumber != null) 'card_number': cardNumber,
      if (expiryMonth != null) 'expiry_month': expiryMonth,
      if (expiryYear != null) 'expiry_year': expiryYear,
      if (cardholderFirstName != null) 'cardholder_first_name': cardholderFirstName,
      if (cardholderLastName != null) 'cardholder_last_name': cardholderLastName,
      if (cardholderEmail != null) 'cardholder_email': cardholderEmail,
      if (cardholderPhoneNumber != null) 'cardholder_phone_number': cardholderPhoneNumber,
      if (cvn != null) 'cvn': cvn,
      if (confirmSave != null) 'confirm_save': confirmSave,
      'payment_session_id': paymentSessionId,
      'device': device.toJson(),
    };
  }
}

/// Device fingerprint information
@immutable
class DeviceFingerprint {
  final String fingerprint;

  const DeviceFingerprint({
    required this.fingerprint,
  });

  Map<String, dynamic> toJson() {
    return {
      'fingerprint': fingerprint,
    };
  }
}

/// Response model for card operations
@immutable
class CardsResponseDto {
  final String message;
  final String? paymentRequestId;
  final String? paymentTokenId;
  final String? actionUrl;

  const CardsResponseDto({
    required this.message,
    this.paymentRequestId,
    this.paymentTokenId,
    this.actionUrl,
  });

  factory CardsResponseDto.fromJson(Map<String, dynamic> json) {
    return CardsResponseDto(
      message: json['message'] as String,
      paymentRequestId: json['payment_request_id'] as String?,
      paymentTokenId: json['payment_token_id'] as String?,
      actionUrl: json['action_url'] as String?,
    );
  }
} 