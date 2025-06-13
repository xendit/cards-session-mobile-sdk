package com.cards.session.util

import io.github.aakira.napier.Napier

class Logger(private val tag: String) {
    fun d(message: String, throwable: Throwable?) {
        Napier.d(message, throwable, tag)
    }

    fun i(message: String, throwable: Throwable?) {
        Napier.i(message, throwable, tag)
    }

    fun w(message: String, throwable: Throwable?) {
        Napier.w(message, throwable, tag)
    }

    fun e(message: String, throwable: Throwable?) {
        Napier.e(message, throwable, tag)
    }

    fun debugBuild() {
    }
}