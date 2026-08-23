plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val naverMapNcpKeyId = providers.gradleProperty("NAVER_MAP_NCP_KEY_ID")
    .orElse("TODO_NCP_KEY_ID")

android {
    namespace = "com.dailytown.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.dailytown.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "NAVER_MAP_NCP_KEY_ID",
            "\"${naverMapNcpKeyId.get().replace("\\", "\\\\").replace("\"", "\\\"")}\"",
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.08.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.2")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.datastore:datastore-preferences:1.2.1")

    // Provider implementation: only this adapter depends on NAVER Maps.
    implementation("com.naver.maps:map-sdk:3.23.3")

    // Location is app-owned and intentionally independent from the map provider.
    implementation("com.google.android.gms:play-services-location:21.3.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
