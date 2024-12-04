plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt)
  alias(libs.plugins.kotlin.serialization)
}


android {
  namespace = "com.cards.session.android"
  compileSdk = 34
  defaultConfig {
    applicationId = "com.cards.session.android"
    minSdk = 21
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlin {
    jvmToolchain(17)
  }
}

dependencies {
  implementation(project(":cardsSdk"))
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.coil.compose)

  implementation(libs.hilt.android)
  ksp(libs.hilt.android.compiler)
  implementation(libs.hilt.navigation.compose)

  implementation(libs.ktor.android)
  implementation(libs.napier)

  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.test.runner)
  androidTestImplementation(libs.test.rule)
//  androidTestImplementation(libs.junit)
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  kspAndroidTest(libs.hilt.android.compiler)
  androidTestImplementation(libs.hilt.testing)
}