# Xendit Cards Session Flutter SDK

A Flutter plugin for integrating Xendit's card payment functionality into your Flutter applications. This SDK provides secure card data collection with built-in validation and 3DS support.

## Features

- Secure card data collection
- CVN/CVV collection
- 3DS support
- Automatic card validation
- Support for both one-time payments and saved cards
- Cross-platform support (Android and iOS)

## Installation

Add this to your package's pubspec.yaml file:

```
dependencies:
  xendit_cards_session: ^1.0.0
```

Then run:

```
flutter pub get
```

## Usage

### Initialize the SDK

```
import 'package:xendit_cards_session/xendit_cards_session.dart';

final xenditCardsSession = XenditCardsSession();

// Initialize with your public API key
await xenditCardsSession.initialize(apiKey: "xnd_public_development_YOUR_KEY_HERE");
```

### Collect Card Data

```
try {
  final response = await xenditCardsSession.collectCardData(
    cardNumber: "4000000000001091",
    expiryMonth: "12",
    expiryYear: "2025",
    cvn: "123", // Optional
    cardholderFirstName: "John",
    cardholderLastName: "Doe",
    cardholderEmail: "john@example.com",
    cardholderPhoneNumber: "+1234567890",
    paymentSessionId: "ps-1234567890", // Session ID from your backend
    confirmSave: true, // Optional, default to false
  );
  
  // Handle successful response
  print("Message: ${response.message}");
  print("Payment Token ID: ${response.paymentTokenId}");
  print("Action URL: ${response.actionUrl}");
  
} catch (e) {
  // Handle error
  print("Error: $e");
}
```

### Collect CVN Only

For saved cards where you only need to collect the CVN:

```
try {
  final response = await xenditCardsSession.collectCvn(
    cvn: "123",
    paymentSessionId: "ps-1234567890",
  );
  
  // Handle successful response
  print("Message: ${response.message}");
  
} catch (e) {
  // Handle error
  print("Error: $e");
}
```

### Monitor Session State

You can listen to the state changes to update your UI accordingly:

```
xenditCardsSession.state.listen((state) {
  if (state.isLoading) {
    // Show loading indicator
  } else if (state.exception != null) {
    // Show error message
    print("Error: ${state.exception!.message}");
  } else if (state.cardResponse != null) {
    // Show success message
    print("Success: ${state.cardResponse!.message}");
  }
});
```

## Response Types

### Success Response
```
{
    "message": "Status updated. Wait for callback",
    "payment_token_id": "pt-1234567890",
    "action_url": "https://redirect-gateway..."
}
```

### Error Response
```
{
    "error_code": "ERROR_CODE",
    "message": "Error message"
}
```

## Security

The SDK automatically handles:
- Secure data transmission
- Card data validation
- 3DS authentication flow
- Fingerprint generation

## Requirements

- Flutter 2.0.0 or higher
- Dart 2.12.0 or higher
- Android API level 21 or higher
- iOS 13.0 or higher

## License

This project is licensed under the MIT License - see the LICENSE file for details.

