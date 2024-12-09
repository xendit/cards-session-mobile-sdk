package com.cards.session.cards.sdk

object CardSessionsIos {
    fun create(apiKey: String): CardSessions {
        return CardSessionsImpl.create(apiKey)
    }
} 