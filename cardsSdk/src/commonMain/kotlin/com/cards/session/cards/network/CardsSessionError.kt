package com.cards.session.cards.network

// TODO map it more cleanly to the exception
@kotlinx.serialization.Serializable
data class CardsErrorResponse(
  val error_code: String,
  val message: String,
  val errors: List<XenError>? = null
)

@kotlinx.serialization.Serializable
data class XenError(
  val path: String,
  val message: String
)

enum class CardsSessionError {
  SERVICE_UNAVAILABLE,
  UNKNOWN_ERROR,
  INVALID_OAUTH_TOKEN,
  INVALID_TOKEN_ERROR
}

class CardsSessionException(
  val errorCode: CardsSessionError,
  val errorMessage: String
) : Exception(
  "An error occurred during cards session: $errorCode ($errorMessage)"
)
