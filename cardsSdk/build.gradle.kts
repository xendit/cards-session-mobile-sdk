import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import java.util.Properties
import java.io.FileInputStream

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.native.cocoapods)
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.serialization)
  `maven-publish`
  signing
}
kotlin {
  androidTarget()
  iosX64()
  iosArm64()
  iosSimulatorArm64()

  val xcframeworkName = "cardSdk"
  val xcFrameworkVersion = "1.0.0"
  val xcFrameworkBundleVersion = "1" // Increase it everytime version changed
  val xcf = XCFramework(xcframeworkName)

  listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64(),
  ).forEach {
    it.binaries.framework {
      baseName = xcframeworkName

      // Specify CFBundleIdentifier to uniquely identify the framework
      binaryOption("bundleId", "com.cards.session.${xcframeworkName}")
      binaryOption("bundleShortVersionString", xcFrameworkVersion)
      binaryOption("bundleVersion", xcFrameworkBundleVersion)
      xcf.add(this)
      isStatic = true
    }
  }

  cocoapods {
    summary = "Cards Session SDK module"
    homepage = "Link to the Cards Session Module homepage"
    version = "1.0"
    ios.deploymentTarget = "14.0"
    podfile = project.file("../iosApp/Podfile")
    framework {
      baseName = "cardsSdk"
      isStatic = true
    }

    pod("XenditFingerprintSDK") {
      version = "1.0.1"
      extraOpts += listOf("-compiler-option", "-fmodules")
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
      isMinifyEnabled = false
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

val localProperties = Properties().apply {
  val localPropertiesFile = rootProject.file("local.properties")
  if (localPropertiesFile.exists()) {
    load(FileInputStream(localPropertiesFile))
  }
}

fun getProperty(propertyName: String): String {
  return localProperties.getProperty(propertyName) ?: System.getenv(propertyName) ?: ""
}

publishing {
  val sonatypeUsername = getProperty("SONATYPE_USERNAME")
  val sonatypePassword = getProperty("SONATYPE_PASSWORD")
  publications.withType<MavenPublication> {
    groupId = "com.xendit"
    version = "1.0.0"

    pom {
      name.set("Xendit Cards SDK")
      description.set("A lightweight SDK for card sessions into Android and iOS applications")
      url.set("https://github.com/xendit/xendit-cards-sdk")

      licenses {
        license {
          name.set("MIT License")
          url.set("https://opensource.org/licenses/MIT")
        }
      }

      developers {
        developer {
          id.set("cards")
          name.set("Xendit Cards")
          email.set("cards@xendit.co")
        }
      }

      scm {
        connection.set("scm:git:git://github.com/xendit/xendit-cards-sdk.git")
        developerConnection.set("scm:git:ssh://github.com/xendit/xendit-cards-sdk.git")
        url.set("https://github.com/xendit/xendit-cards-sdk")
      }
    }
  }

  repositories {
    maven {
      name = "OSSRH"
      val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
      val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
      url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)

      credentials {
        username = sonatypeUsername
        password = sonatypePassword
      }
    }
  }
}

tasks.withType<PublishToMavenRepository>().configureEach {
  val targetName = publication.name
  if (targetName.contains("ios", ignoreCase = true)) {
    enabled = false
  }
}

tasks.withType<Sign>().configureEach {
  val targetName = name
  if (targetName.contains("ios", ignoreCase = true)) {
    enabled = false
  }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}
