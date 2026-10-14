import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Nomor build otomatis dari GitHub Actions (1 kalau di-build manual)
val buildNumber = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toInt()

android {
    namespace = "com.valeriusgames.threadsofdestiny"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.valeriusgames.threadsofdestiny"
        minSdk = 26
        targetSdk = 35
        versionCode = buildNumber
        versionName = "1.0.0"
    }

    signingConfigs {
        // Keystore tetap di repo, supaya APK baru bisa di-install menimpa APK lama
        getByName("debug") {
            storeFile = rootProject.file("keystore/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    androidResources {
        // Video jangan dikompres ulang saat dikemas
        noCompress += "mp4"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}
