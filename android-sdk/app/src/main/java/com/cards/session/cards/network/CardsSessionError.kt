package com.cards.session.cards.network

import kotlinx.serialization.Serializable

@Serializable
data class CardsErrorResponse(
  val error_code: String,
  val message: String,
  val errors: List<XenError>? = null
)

@Serializable
data class XenError(
  val path: String,
  val message: String
)

enum class CardsSessionError {
  SERVICE_UNAVAILABLE,
  INVALID_OAUTH_TOKEN,
  INVALID_TOKEN_ERROR,
  SERVER_ERROR,
  API_VALIDATION_ERROR,
  UNKNOWN_ERROR
}

class CardsSessionException(
  val errorCode: CardsSessionError,
  val errorMessage: String
) : Exception(
  "An error occurred during cards session: $errorCode ($errorMessage)"
)
