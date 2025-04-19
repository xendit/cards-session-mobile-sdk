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

  @SerialName("billing_information")
  val billingInformation: BillingInformationDto? = null,

  @SerialName("payment_session_id")
  val paymentSessionId: String,
  val device: DeviceFingerprint
)

@Serializable
data class BillingInformationDto(
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    val email: String,
    @SerialName("phone_number")
    val phoneNumber: String? = null,
    @SerialName("street_line1")
    val streetLine1: String,
    @SerialName("street_line2")
    val streetLine2: String? = null,
    val city: String,
    @SerialName("province_state")
    val provinceState: String,
    val country: String,
    @SerialName("postal_code")
    val postalCode: String
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