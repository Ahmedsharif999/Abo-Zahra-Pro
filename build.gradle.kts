plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.hammelplus.downloader"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hammelplus.downloader"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1"
    }
}
