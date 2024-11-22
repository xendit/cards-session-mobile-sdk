package com.cards.session

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform