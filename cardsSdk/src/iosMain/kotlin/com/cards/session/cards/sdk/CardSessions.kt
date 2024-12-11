package com.cards.session.cards.sdk

actual fun CardSessions.Factory.create(apiKey: String): CardSessions {
    return CardSessionsImpl.create(apiKey)
}

fun CardSessions.Factory.create(apiKey: String, isEnableLog: Boolean): CardSessions {
    return CardSessionsImpl.create(apiKey, isEnableLog)
}