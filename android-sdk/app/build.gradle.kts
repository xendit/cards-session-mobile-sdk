import java.io.FileInputStream
import java.util.Properties

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.serialization)
  `maven-publish`
  signing
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}

fun getProperty(propertyName: String): String {
    return localProperties.getProperty(propertyName)
        ?: System.getenv(propertyName)
        ?: ""
}

kotlin {
    jvmToolchain(17)
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
    }
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      consumerProguardFiles("proguard-rules.pro")
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
  implementation(libs.bundles.ktor)
  implementation(libs.ktor.android)
  implementation(libs.kotlin.date.time)
  implementation(libs.napier)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.xendit.fingerprint)
  implementation(libs.material)

  coreLibraryDesugaring(libs.android.desugar)
}

publishing {
    val sonatypeUsername = getProperty("SONATYPE_USERNAME")
    val sonatypePassword = getProperty("SONATYPE_PASSWORD")

    publications {
        register<MavenPublication>("releaseAar") {
            groupId = "com.xendit"
            artifactId = "cardsSdk"
            version = "1.1.0"

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("Xendit Cards SDK")
                description.set("A lightweight SDK for card sessions into Android applications")
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
    }

    repositories {
        mavenLocal()
        maven {
            name = "OSSRH"
            setUrl(provider {
                val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                val publicationVersion = (publications["releaseAar"] as MavenPublication).version
                if (publicationVersion.endsWith("-SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            })

            credentials {
                username = sonatypeUsername
                password = sonatypePassword
            }
        }
    }
}

signing {
    val shouldSign = !getProperty("signing.keyId").isNullOrBlank()
    if (shouldSign) {
        sign(publishing.publications)
    }
}

ext["signing.keyId"] = getProperty("signing.keyId")
ext["signing.password"] = getProperty("signing.password")
ext["signing.secretKeyRingFile"] = project.rootProject.file("secring.gpg").absolutePath