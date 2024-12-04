package com.cards.session.cards.models

import kotlinx.serialization.Serializable

// API will not accept null values, use string instead for all optional fields
@Serializable
data class CardsRequestDto(
  // required for collectCardData()
  val card_number: String = "",
  val expiry_month: String = "",
  val expiry_year: String = "",
  val cardholder_first_name: String = "",
  val cardholder_last_name: String = "",
  val cardholder_email: String = "",
  val cardholder_phone_number: String = "",

  // required for collectCvn()
  val cvn: String = "",

  // always required
  val payment_session_id: String,
  val device: DeviceFingerprint
)

@Serializable
data class DeviceFingerprint(
  var fingerprint: String = ""
)

@Serializable
data class CardsResponseDto(
  val message: String,
  val payment_request_id: String? = null, // required for collectCvn
  val payment_token_id: String? = null, // collectCardData will require either this or payment_request_id
  val action_url: String? = null // not required for any
)