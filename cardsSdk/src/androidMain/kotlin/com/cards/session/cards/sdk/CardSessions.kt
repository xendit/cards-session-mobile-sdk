package com.cards.session.cards.sdk

import android.content.Context

actual fun CardSessions.Factory.create(apiKey: String): CardSessions {
    throw IllegalStateException("On Android, use create(context, apiKey)")
}

fun CardSessions.Factory.create(context: Context, apiKey: String): CardSessions {
    return CardSessionsImpl.create(context, apiKey)
}