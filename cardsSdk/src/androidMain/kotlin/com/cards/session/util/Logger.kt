package com.cards.session.util

import io.github.aakira.napier.Napier

actual class Logger actual constructor(private val tag: String) {
    actual fun d(message: String, throwable: Throwable?) {
        Napier.d(message, throwable, tag)
    }

    actual fun i(message: String, throwable: Throwable?) {
        Napier.i(message, throwable, tag)
    }

    actual fun w(message: String, throwable: Throwable?) {
        Napier.w(message, throwable, tag)
    }

    actual fun e(message: String, throwable: Throwable?) {
        Napier.e(message, throwable, tag)
    }

    actual fun debugBuild() {
    }
} 