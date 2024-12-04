package com.cards.session.cards.ui

sealed class CardSessionEvent {
  data class CollectCardData(
    val cardNumber: String,
    val expiryMonth: String,
    val expiryYear: String,
    val cardholderFirstName: String,
    val cardholderLastName: String,
    val cardholderEmail: String,
    val cardholderPhoneNumber: String,
    val paymentSessionId: String,
    val deviceFingerprint: String = ""
  ) : CardSessionEvent()

  data class CollectCvn(
    val cvn: String,
    val paymentSessionId: String,
    val deviceFingerprint: String = ""
  ) : CardSessionEvent()

  object OnErrorSeen : CardSessionEvent()
}