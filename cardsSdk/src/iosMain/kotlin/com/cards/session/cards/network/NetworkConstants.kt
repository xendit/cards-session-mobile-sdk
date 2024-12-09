package com.cards.session.cards.network

import platform.Foundation.NSBundle

actual object NetworkConstants {
  actual val BASE_URL: String
    get() = if (isDebugBuild()) "https://api.stg.tidnex.dev/v3" else "https://api.xendit.co/v3"

  // please check this logic
  private fun isDebugBuild(): Boolean {
    val bundle = NSBundle.mainBundle
    return bundle.infoDictionary?.get("Debug") as? Boolean ?: false
  }
}