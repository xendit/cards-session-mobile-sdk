package com.cards.session.util

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

actual class Logger actual constructor(private val tag: String) {
  actual fun d(message: String, throwable: Throwable?) {
    Napier.d(message = "$message${throwable?.let { "\n$it" } ?: ""}", tag = tag)
  }

  actual fun i(message: String, throwable: Throwable?) {
    Napier.i(message = "$message${throwable?.let { "\n$it" } ?: ""}", tag = tag)
  }

  actual fun w(message: String, throwable: Throwable?) {
    Napier.w(message = "$message${throwable?.let { "\n$it" } ?: ""}", tag = tag)
  }

  actual fun e(message: String, throwable: Throwable?) {
    Napier.e(message = "$message${throwable?.let { "\n$it" } ?: ""}", tag = tag)
  }

  actual fun debugBuild() {
    Napier.base(DebugAntilog())
  }
}