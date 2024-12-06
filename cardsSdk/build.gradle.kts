plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.native.cocoapods)
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.serialization)
}
kotlin {
  androidTarget()
  iosX64()
  iosArm64()
  iosSimulatorArm64()

  cocoapods {
    summary = "Cards Session SDK module"
    homepage = "Link to the Cards Session Module homepage"
    version = "1.0"
    ios.deploymentTarget = "16.0"
    podfile = project.file("../iosApp/Podfile")
    framework {
      baseName = "cardsSdk"
      isStatic = true
    }
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(libs.bundles.ktor)
        implementation(libs.kotlin.date.time)
        implementation(libs.napier)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.serialization.json)
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
        implementation(libs.assertk)
        implementation(libs.turbine)
      }
    }
    val androidMain by getting {
      dependencies {
        implementation(libs.ktor.android)
        implementation(libs.xendit.fingerprint)
      }
    }
    val iosX64Main by getting
    val iosArm64Main by getting
    val iosSimulatorArm64Main by getting
    val iosMain by creating {
      dependsOn(commonMain)
      iosX64Main.dependsOn(this)
      iosArm64Main.dependsOn(this)
      iosSimulatorArm64Main.dependsOn(this)

      dependencies {
        implementation(libs.ktor.ios)
      }
    }
    val iosX64Test by getting
    val iosArm64Test by getting
    val iosSimulatorArm64Test by getting
    val iosTest by creating {
      dependsOn(commonTest)
      iosX64Test.dependsOn(this)
      iosArm64Test.dependsOn(this)
      iosSimulatorArm64Test.dependsOn(this)
    }
  }
}

android {
  namespace = "com.cards.session"
  compileSdk = 34
  defaultConfig {
    minSdk = 21
  }

  buildTypes {
    debug {
      isMinifyEnabled = false
      buildConfigField("String", "BASE_URL", "\"https://api.stg.tidnex.dev/v3\"")
    }
    release {
      isMinifyEnabled = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
      consumerProguardFiles("proguard-rules.pro")
      buildConfigField("String", "BASE_URL", "\"https://api.xendit.co/v3\"")
    }
  }

  compileOptions {
    isCoreLibraryDesugaringEnabled = true
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildFeatures {
    buildConfig = true
  }
}

dependencies {
  coreLibraryDesugaring(libs.android.desugar)
}
