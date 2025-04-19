package com.cards.session.cards.ui

import com.cards.session.cards.models.BillingInformationDto

sealed class CardSessionEvent {
  data class CollectCardData(
    val cardNumber: String,
    val expiryMonth: String,
    val expiryYear: String,
    val cvn: String?,
    val cardholderFirstName: String,
    val cardholderLastName: String,
    val cardholderEmail: String,
    val cardholderPhoneNumber: String,
    val paymentSessionId: String,
    val confirmSave: Boolean = false,
    val billingInformation: BillingInformationDto? = null,
    val deviceFingerprint: String = ""
  ) : CardSessionEvent()

  data class CollectCvn(
    val cvn: String,
    val paymentSessionId: String,
    val deviceFingerprint: String = ""
  ) : CardSessionEvent()

  object OnErrorSeen : CardSessionEvent()
}