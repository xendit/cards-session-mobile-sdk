package com.cards.session

class Greeting {
    private val platform: Platform = com.cards.session.getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}