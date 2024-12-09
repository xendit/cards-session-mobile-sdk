package com.cards.session.util

import io.github.aakira.napier.Napier

expect class Logger(tag: String) {
    fun d(message: String, throwable: Throwable? = null)
    fun i(message: String, throwable: Throwable? = null)
    fun w(message: String, throwable: Throwable? = null)
    fun e(message: String, throwable: Throwable? = null)
    fun debugBuild()
}