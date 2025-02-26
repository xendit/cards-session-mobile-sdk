/// Utility class for credit card validation
class CreditCardUtil {
  /// Validates if a credit card number is valid
  /// 
  /// Currently just checks if the number is not empty
  /// TODO: Implement full validation logic including Luhn algorithm
  static bool isCreditCardNumberValid(String cardNumber) {
    return cardNumber.trim().isNotEmpty;
  }

  /// Validates if a credit card expiration date is valid
  /// 
  /// Checks if month is between 1-12 and year is not in the past
  static bool isCreditCardExpirationDateValid(String month, String year) {
    final monthInt = int.tryParse(month);
    final yearInt = int.tryParse(year);
    
    if (monthInt == null || yearInt == null) {
      return false;
    }
    
    // Check if month is valid
    if (monthInt < 1 || monthInt > 12) {
      return false;
    }
    
    // Get current date for comparison
    final now = DateTime.now();
    final currentYear = now.year;
    
    // Check if year is valid (not in the past)
    return yearInt >= currentYear;
  }

  /// Validates if a credit card CVN is valid
  /// 
  /// CVN is optional, but if provided must be 3-4 digits
  static bool isCreditCardCVNValid(String? cvn) {
    if (cvn == null) return true; // CVN is optional
    if (cvn.trim().isEmpty) return false;
    
    final cvnDigits = cvn.trim();
    return cvnDigits.length >= 3 && 
           cvnDigits.length <= 4 && 
           cvnDigits.codeUnits.every((c) => c >= 48 && c <= 57); // Check if all characters are digits
  }
} 