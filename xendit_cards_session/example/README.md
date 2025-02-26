# Xendit Cards Session Example

A Flutter application demonstrating how to use the Xendit Cards Session plugin for secure card payment processing.

## Overview

This example app shows how to:

1. Initialize the Xendit Cards Session SDK
2. Collect complete card information
3. Collect CVN only (for saved cards)
4. Monitor the state of the payment session
5. Handle success and error responses

## Getting Started

### Prerequisites

- Flutter SDK installed
- Android Studio or Xcode for running on devices/emulators
- A Xendit account with API keys

### Running the Example

1. Clone the repository
2. Navigate to the example directory
3. Update the API key in the `_initializeSDK()` method with your Xendit public key
4. Run the app:

```
flutter run
```

## Implementation Details

The example demonstrates a simple card payment flow:

1. Enter a payment session ID (this would typically come from your backend)
2. Toggle whether to save the card for future use
3. Choose to either collect full card data or just the CVN
4. View the response or error message