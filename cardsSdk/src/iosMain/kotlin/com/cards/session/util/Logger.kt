package com.cards.session.util

import platform.Foundation.NSLog

actual class Logger actual constructor(private val tag: String) {
    actual fun d(message: String, throwable: Throwable?) {
        NSLog("$tag [DEBUG]: $message${throwable?.let { "\n$it" } ?: ""}")
    }

    actual fun i(message: String, throwable: Throwable?) {
        NSLog("$tag [INFO]: $message${throwable?.let { "\n$it" } ?: ""}")
    }

    actual fun w(message: String, throwable: Throwable?) {
        NSLog("$tag [WARN]: $message${throwable?.let { "\n$it" } ?: ""}")
    }

    actual fun e(message: String, throwable: Throwable?) {
        NSLog("$tag [ERROR]: $message${throwable?.let { "\n$it" } ?: ""}")
    }
} 