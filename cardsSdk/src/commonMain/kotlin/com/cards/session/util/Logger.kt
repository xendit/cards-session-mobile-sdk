package com.cards.session.util

import io.github.aakira.napier.Napier

class Logger(private val tag: String) {
    fun debug(message: String, throwable: Throwable? = null) {
        Napier.d(message, throwable, tag)
    }

    fun info(message: String, throwable: Throwable? = null) {
        Napier.i(message, throwable, tag)
    }

    fun warning(message: String, throwable: Throwable? = null) {
        Napier.w(message, throwable, tag)
    }

    fun error(message: String, throwable: Throwable? = null) {
        Napier.e(message, throwable, tag)
    }
}