# Xendit Cards Plugin

A Flutter plugin for integrating Xendit Cards SDK into your Flutter applications. This plugin provides secure card data collection functionality with built-in validation and 3DS support.

## Features

- Secure card data collection
- CVN/CVV collection
- 3DS support
- Automatic card validation
- Support for both one-time payments and saved cards

## Installation

Add this to your package's pubspec.yaml file:

```yaml
dependencies:
  xendit_cards_plugin: ^1.0.0
```

## Usage

### Initialize the Plugin

```dart
final xenditCardsPlugin = XenditCardsPlugin(
  apiKey: 'xnd_public_development_YOUR_KEY_HERE'
);
```

### Collect Card Data

```dart
try {
  final response = await xenditCardsPlugin.collectCardData(
    cardNumber: '4242424242424242',
    expiryMonth: '12',
    expiryYear: '2025',
    firstName: 'John',
    lastName: 'Doe',
    email: 'john@example.com',
    phoneNumber: '+1234567890',
    paymentSessionId: 'ps-1234567890'
  );
  
  print('Success: ${response.message}');
} catch (e) {
  print('Error: $e');
}
```

### Collect CVN Only

For saved cards where you only need to collect the CVN:

```dart
try {
  final response = await xenditCardsPlugin.collectCvn(
    cvn: '123',
    paymentSessionId: 'ps-1234567890'
  );
  
  print('Success: ${response.message}');
} catch (e) {
  print('Error: $e');
}
```

## Response Types

### Success Response
```json
{
    "message": "Status updated. Wait for callback",
    "payment_token_id": "pt-1234567890",
    "action_url": "https://redirect-gateway..."
}
```

### Error Response
```json
{
    "error_code": "ERROR_CODE",
    "message": "Error message"
}
```

## Requirements

- Flutter 3.3.0 or higher
- Android API level 21 or higher
- iOS 14.0 or higher

## Example

Check out the [example](example) directory for a complete sample app.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

