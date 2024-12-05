package com.cards.session.cards.network

import com.cards.session.BuildConfig

actual object NetworkConstants {
  actual val PROD_URL: String = "https://api.xendit.co/v3"
  actual val STG_URL: String = "https://api.stg.tidnex.dev/v3"
  actual val BASE_URL: String
    get() = if (BuildConfig.DEBUG) STG_URL else PROD_URL
}