package com.cards.session.cards.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CardsRequestDto(
  // required for collectCardData()
  @SerialName("card_number")
  val cardNumber: String? = null,
  @SerialName("expiry_month")
  val expiryMonth: String? = null,
  @SerialName("expiry_year")
  val expiryYear: String? = null,
  @SerialName("cardholder_first_name")
  val cardholderFirstName: String? = null,
  @SerialName("cardholder_last_name")
  val cardholderLastName: String? = null,
  @SerialName("cardholder_email")
  val cardholderEmail: String? = null,
  @SerialName("cardholder_phone_number")
  val cardholderPhoneNumber: String? = null,

  // required for collectCvn()
  val cvn: String? = null,

  @SerialName("confirm_save")
  val confirmSave: Boolean? = false,

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