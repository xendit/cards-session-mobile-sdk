package com.cards.session.cards.network

import platform.Foundation.NSBundle

actual object NetworkConstants {
    actual val PROD_URL: String = "https://api.xendit.co/v3"
    actual val STG_URL: String = "https://api.stg.tidnex.dev/v3"
    actual val BASE_URL: String
        get() = if (isDebugBuild()) STG_URL else PROD_URL

    private fun isDebugBuild(): Boolean {
        val bundle = NSBundle.mainBundle
        return bundle.infoDictionary?.get("Debug") as? Boolean ?: false
    }
} 