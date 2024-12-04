package com.cards.session.cards.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CardsRequestDto(
  // required for collectCardData()
  @SerialName("card_number")
  val cardNumber: String = "",
  @SerialName("expiry_month")
  val expiryMonth: String = "",
  @SerialName("expiry_year")
  val expiryYear: String = "",
  @SerialName("cardholder_first_name")
  val cardholderFirstName: String = "",
  @SerialName("cardholder_last_name")
  val cardholderLastName: String = "",
  @SerialName("cardholder_email")
  val cardholderEmail: String = "",
  @SerialName("cardholder_phone_number")
  val cardholderPhoneNumber: String = "",

  // required for collectCvn()
  val cvn: String? = null,

  @SerialName("payment_session_id")
  val paymentSessionId: String,
  val device: DeviceFingerprint
)

@Serializable
data class DeviceFingerprint(
  var fingerprint: String = ""
)

@Serializable
data class CardsResponseDto(
  val message: String,
  @SerialName("payment_request_id")
  val paymentRequestId: String? = null, // required for collectCvn
  @SerialName("payment_token_id")
  val paymentTokenId: String? = null, // collectCardData will require either this or payment_request_id
  @SerialName("action_url")
  val actionUrl: String? = null // not required for any
)