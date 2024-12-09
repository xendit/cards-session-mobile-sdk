# Xendit Cards SDK

A lightweight SDK for integrating card payments into Android and iOS applications. This SDK provides secure card data collection functionality with built-in validation and 3DS support.

## Features

- Secure card data collection
- CVN/CVV collection
- 3DS support
- Automatic card validation
- Support for both one-time payments and saved cards

## Installation

### Android - Gradle

Add the following to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.xendit:cards-sdk:1.0.0") // Replace with actual version
}
```

## Usage

### Initialize the SDK

#### Android
```kotlin
val cardSessions = CardSessions.create(
    context = context,
    apiKey = "xnd_public_development_YOUR_KEY_HERE"
)
```

#### iOS
```swift
let cardSessions = CardSessionsFactory().create(
    apiKey: "xnd_public_development_YOUR_KEY_HERE"
)
```

### Collect Card Data

#### Android
```kotlin
// Collect complete card information
val response = cardSessions.collectCardData(
    cardNumber = "4000000000001091",
    expiryMonth = "12",
    expiryYear = "2025",
    cvn = "123", // Optional
    cardholderFirstName = "John",
    cardholderLastName = "Doe",
    cardholderEmail = "john@example.com",
    cardholderPhoneNumber = "+1234567890",
    paymentSessionId = "ps-1234567890" // Session ID from your backend
)
```

#### iOS
```swift
// Using async/await
let response = try await cardSessions.collectCardData(
    cardNumber: "4000000000001091",
    expiryMonth: "12",
    expiryYear: "2025",
    cvn: "123", // Optional
    cardholderFirstName: "John",
    cardholderLastName: "Doe",
    cardholderEmail: "john@example.com",
    cardholderPhoneNumber: "+1234567890",
    paymentSessionId: "ps-1234567890"
)
```

### Collect CVN Only

For saved cards where you only need to collect the CVN:

#### Android
```kotlin
val response = cardSessions.collectCvn(
    cvn = "123",
    paymentSessionId = "ps-1234567890"
)
```

#### iOS
```swift
let response = try await cardSessions.collectCvn(
    cvn: "123",
    paymentSessionId: "ps-1234567890"
)
```

### Monitor Session State

#### Android
```kotlin
cardSessions.state.collect { state ->
    when {
        state.isLoading -> // Show loading state
        state.error != null -> // Handle error
        state.cardResponse != null -> // Handle success
    }
}
```

#### iOS
```swift
// Using Combine
cardSessions.state
    .sink { state in
        if state.isLoading {
            // Show loading state
        } else if let error = state.error {
            // Handle error
        } else if let response = state.cardResponse {
            // Handle success
        }
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

## Security

The SDK automatically handles:
- Secure data transmission
- Card data validation
- 3DS authentication flow
- Fingerprint generation

## Requirements

- Android API level 21 or higher
- iOS 13.0 or higher
- Swift 5.5 or higher

