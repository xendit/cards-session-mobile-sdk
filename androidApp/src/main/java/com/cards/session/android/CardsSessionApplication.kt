package com.cards.session.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

@HiltAndroidApp
class CardsSessionApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    Napier.base(DebugAntilog())
  }
}