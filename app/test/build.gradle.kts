plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.fastjien.sunny.test"

    defaultConfig {
        applicationId = "com.fastjien.sunny.test"
        versionCode = 1
        versionName = "1.0"
        proguardFile("proguard-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
        }
    }
}

setupTestApk()

dependencies {
    implementation(libs.test.runner)
    implementation(libs.test.rules)
    implementation(libs.test.junit)
    implementation(libs.test.uiautomator)
}

tasks.matching { it.name == "generateDebugLintReportModel" }.configureEach {
    dependsOn("downloadDebugLsposed")
}

tasks.matching { it.name == "lintAnalyzeDebug" }.configureEach {
    dependsOn("downloadDebugLsposed")
}