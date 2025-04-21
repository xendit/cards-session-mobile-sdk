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
  final BillingInformationDto? billingInformation;
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
    this.billingInformation,
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
      if (billingInformation != null) 'billing_information': billingInformation!.toJson(),
      'payment_session_id': paymentSessionId,
      'device': device.toJson(),
    };
  }
}

/// Billing information
@immutable
class BillingInformationDto {
  final String firstName;
  final String lastName;
  final String email;
  final String? phoneNumber;
  final String streetLine1;
  final String? streetLine2;
  final String city;
  final String provinceState;
  final String country;
  final String postalCode;

  const BillingInformationDto({
    required this.firstName,
    required this.lastName,
    required this.email,
    this.phoneNumber,
    required this.streetLine1,
    this.streetLine2,
    required this.city,
    required this.provinceState,
    required this.country,
    required this.postalCode,
  });

  Map<String, dynamic> toJson() {
    return {
      'first_name': firstName,
      'last_name': lastName,
      'email': email,
      if (phoneNumber != null) 'phone_number': phoneNumber,
      'street_line1': streetLine1,
      if (streetLine2 != null) 'street_line2': streetLine2,
      'city': city,
      'province_state': provinceState,
      'country': country,
      'postal_code': postalCode,
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