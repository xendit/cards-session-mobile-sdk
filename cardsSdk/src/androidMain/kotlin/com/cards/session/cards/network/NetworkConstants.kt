package com.cards.session.cards.network

import com.cards.session.BuildConfig

actual object NetworkConstants {
    actual val BASE_URL: String
        get() = BuildConfig.BASE_URL
}