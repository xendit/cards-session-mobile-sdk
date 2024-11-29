package com.cards.session.android

import android.app.Application
import com.xendit.fingerprintsdk.XenditFingerprintSDK
import dagger.hilt.android.HiltAndroidApp
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

@HiltAndroidApp
class CardsSessionApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    Napier.base(DebugAntilog())
    
    // TODO user should enter proper api key
    XenditFingerprintSDK.init(
      this,
      "TODO"
    )
  }
}