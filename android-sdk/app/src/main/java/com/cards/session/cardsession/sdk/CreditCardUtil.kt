package com.cardsession.sdk

/**
 * Utility class for credit card validation
 * TODO Implement all of https://github.com/xendit/xendit.js/blob/26106eef2939870058ee6c219ac837d4c45b7fcf/src/utils/credit_card_util.js
 */
object CreditCardUtil {
  fun isCreditCardNumberValid(cardNumber: String): Boolean {
    return cardNumber.isNotBlank()
  }

  fun isCreditCardExpirationDateValid(month: String, year: String): Boolean {
    val monthInt = month.toIntOrNull() ?: return false
    val yearInt = year.toIntOrNull() ?: return false

    return monthInt in 1..12 && yearInt >= 2024
  }

  fun isCreditCardCVNValid(cvn: String?): Boolean {
    if (cvn == null) return true // CVN is optional
    if (cvn.isBlank()) return false
    val cvnDigits = cvn.trim()
    return cvnDigits.length in 3..4 && cvnDigits.all { it.isDigit() }
  }
} 