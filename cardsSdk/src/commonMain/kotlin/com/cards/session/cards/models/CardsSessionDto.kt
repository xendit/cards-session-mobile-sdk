package com.cards.session.cards.models

@kotlinx.serialization.Serializable
data class CardsRequestDto(
  // required for collectCardData()
  val card_number: String? = null,
  val expiry_month: String? = null,
  val expiry_year: String? = null,
  val cardholder_first_name: String? = null,
  val cardholder_last_name: String? = null,
  val cardholder_email: String? = null,
  val cardholder_phone_number: String? = null,

  // required for collectCvn()
  val cvn: String? = null,

  // always required
  val payment_session_id: String,
  val device_fingerprint: String
)

@kotlinx.serialization.Serializable
data class CardsResponseDto(
  val message: String,
  val payment_request_id: String? = null, // required for collectCvn
  val payment_token_id: String? = null, // collectCardData will require either this or payment_request_id
  val action_url: String? = null // not required for any
)