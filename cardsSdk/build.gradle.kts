import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFrameworkTask
import java.nio.file.Files

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

  val xcframeworkName = "cardsSdk"
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
    homepage = "https://github.com/xendit/cards-session-mobile-sdk"
    version = "1.0.0"
    license = "{ :type => 'MIT', :text => 'License text'}"
    ios.deploymentTarget = "14.0"
    source = "{\n" +
        "    http: 'PUT THE URL Github Asset here'\n" +
        "  }"
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

interface Injected {
  @get:Inject val fs: FileSystemOperations
}

// Define a custom task type to handle the file copying
abstract class CopyPrivacyInfoTask : DefaultTask(), Injected {
  @get:InputFiles
  abstract val xcframeworkOutputs: ConfigurableFileCollection

  @get:InputFile
  abstract val privacyFile: RegularFileProperty

  @get:OutputDirectory
  abstract val outputDir: DirectoryProperty

  @TaskAction
  fun copy() {
    val xcframework = xcframeworkOutputs.first().toPath()
    Files.find(xcframework, 2, { path, _ ->
      val isFramework = path.fileName.toString().endsWith(".framework")
      val destination = path.getName(path.count() - 2).fileName.toString()
      val isIOS = destination.startsWith("ios-")
      isFramework && isIOS
    }).forEach { framework ->
      fs.copy {
        from(privacyFile)
        into(framework)
      }
    }
  }
}

// Register the custom task
tasks.register<CopyPrivacyInfoTask>("copyPrivacyInfoToFrameworks") {
  val xcframeworkTask = tasks.named<XCFrameworkTask>("podPublishReleaseXCFramework")
  xcframeworkOutputs.from(xcframeworkTask.map { it.outputs.files })
  privacyFile.set(project.file("PrivacyInfo.xcprivacy"))
  outputDir.set(layout.buildDirectory.dir("xcframework"))

  // Make this task run after XCFramework task
  dependsOn(xcframeworkTask)
}

// Make XCFramework task finalized by our copy task
tasks.named<XCFrameworkTask>("podPublishReleaseXCFramework") {
  finalizedBy("copyPrivacyInfoToFrameworks")
}

