plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val naverMapNcpKeyId = providers.gradleProperty("NAVER_MAP_NCP_KEY_ID")
    .orElse(providers.environmentVariable("NAVER_MAP_NCP_KEY_ID"))
    .orElse("TODO_NCP_KEY_ID")
val resolvedNaverMapNcpKeyId = naverMapNcpKeyId.get()
val naverMapConfigured = resolvedNaverMapNcpKeyId.isNotBlank() && !resolvedNaverMapNcpKeyId.startsWith("TODO_")

fun optionalConfig(name: String): String = providers.gradleProperty(name)
    .orElse(providers.environmentVariable(name))
    .orElse("")
    .get()
    .trim()

fun optionalNonNegativeLong(name: String): Long {
    val raw = optionalConfig(name)
    if (raw.isBlank()) return -1L
    return raw.toLongOrNull()?.takeIf { it >= 0L }
        ?: error("$name must be a non-negative integer when configured.")
}

fun optionalNonNegativeInt(name: String): Int {
    val raw = optionalConfig(name)
    if (raw.isBlank()) return -1
    return raw.toIntOrNull()?.takeIf { it >= 0 }
        ?: error("$name must be a non-negative integer when configured.")
}

fun optionalPositiveInt(name: String): Int {
    val raw = optionalConfig(name)
    if (raw.isBlank()) return -1
    return raw.toIntOrNull()?.takeIf { it > 0 }
        ?: error("$name must be a positive integer when configured.")
}

fun optionalPercent(name: String): Int {
    val raw = optionalConfig(name)
    if (raw.isBlank()) return -1
    return raw.toIntOrNull()?.takeIf { it in 0..100 }
        ?: error("$name must be an integer from 0 to 100 when configured.")
}

fun optionalBoolean(name: String): Boolean {
    val raw = optionalConfig(name)
    if (raw.isBlank()) return false
    return when (raw.lowercase()) {
        "true" -> true
        "false" -> false
        else -> error("$name must be true or false when configured.")
    }
}

fun optionalTriStateBoolean(name: String): Int {
    val raw = optionalConfig(name)
    if (raw.isBlank()) return -1
    return when (raw.lowercase()) {
        "true" -> 1
        "false" -> 0
        else -> error("$name must be true or false when configured.")
    }
}

fun optionalComparisonEvidence(name: String): String {
    val raw = optionalConfig(name)
    if (raw.isBlank()) return ""
    val allowed = setOf(
        "SESSION_DURATION",
        "SESSION_DISTANCE",
        "GPS_REJECTION_RATE",
        "DISTANCE_ERROR",
        "BATTERY_DRAIN",
        "DISCOVERED_ENCOUNTERS",
        "ENCOUNTER_RESOLUTION",
        "REVISIT_SHARE",
        "REPEAT_AREA_FATIGUE",
    )
    val values = raw.split(',')
        .map { it.trim().uppercase() }
        .filter { it.isNotEmpty() }
        .distinct()
        .sorted()
    val invalid = values.filterNot { it in allowed }
    check(invalid.isEmpty()) {
        "$name contains unsupported evidence keys: ${invalid.joinToString(",")}"
    }
    return values.joinToString(",")
}

val fieldTestMinSessionSeconds = optionalNonNegativeLong("FIELD_TEST_MIN_SESSION_SECONDS")
val fieldTestMaxGpsRejectionPercent = optionalPercent("FIELD_TEST_MAX_GPS_REJECTION_PERCENT")
val fieldTestRequireMapReady = optionalBoolean("FIELD_TEST_REQUIRE_MAP_READY")
val fieldTestMaxDistanceErrorPercent = optionalPercent("FIELD_TEST_MAX_DISTANCE_ERROR_PERCENT")
val fieldTestMaxBatteryDrainPercentPerHour = optionalNonNegativeInt("FIELD_TEST_MAX_BATTERY_DRAIN_PERCENT_PER_HOUR")
val fieldTestMinEncountersPerSession = optionalNonNegativeInt("FIELD_TEST_MIN_ENCOUNTERS_PER_SESSION")
val fieldTestMinEncounterResolutionPercent = optionalPercent("FIELD_TEST_MIN_ENCOUNTER_RESOLUTION_PERCENT")
val fieldTestMaxRepeatAreaFatiguePercent = optionalPercent("FIELD_TEST_MAX_REPEAT_AREA_FATIGUE_PERCENT")
val fieldTestComparisonMinSessionsPerCohort = optionalPositiveInt("FIELD_TEST_COMPARISON_MIN_SESSIONS_PER_COHORT")
val fieldTestComparisonRequireMatchingPreset = optionalTriStateBoolean("FIELD_TEST_COMPARISON_REQUIRE_MATCHING_PRESET")
val fieldTestComparisonRequiredEvidence = optionalComparisonEvidence("FIELD_TEST_COMPARISON_REQUIRED_EVIDENCE")

android {
    namespace = "com.dailytown.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.dailytown.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 7
        versionName = "0.7.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "NAVER_MAP_NCP_KEY_ID",
            "\"${resolvedNaverMapNcpKeyId.replace("\\", "\\\\").replace("\"", "\\\"")}\"",
        )
        buildConfigField("boolean", "NAVER_MAP_CONFIGURED", naverMapConfigured.toString())
        buildConfigField("long", "FIELD_TEST_MIN_SESSION_SECONDS", "${fieldTestMinSessionSeconds}L")
        buildConfigField("int", "FIELD_TEST_MAX_GPS_REJECTION_PERCENT", fieldTestMaxGpsRejectionPercent.toString())
        buildConfigField("boolean", "FIELD_TEST_REQUIRE_MAP_READY", fieldTestRequireMapReady.toString())
        buildConfigField("int", "FIELD_TEST_MAX_DISTANCE_ERROR_PERCENT", fieldTestMaxDistanceErrorPercent.toString())
        buildConfigField("int", "FIELD_TEST_MAX_BATTERY_DRAIN_PERCENT_PER_HOUR", fieldTestMaxBatteryDrainPercentPerHour.toString())
        buildConfigField("int", "FIELD_TEST_MIN_ENCOUNTERS_PER_SESSION", fieldTestMinEncountersPerSession.toString())
        buildConfigField("int", "FIELD_TEST_MIN_ENCOUNTER_RESOLUTION_PERCENT", fieldTestMinEncounterResolutionPercent.toString())
        buildConfigField("int", "FIELD_TEST_MAX_REPEAT_AREA_FATIGUE_PERCENT", fieldTestMaxRepeatAreaFatiguePercent.toString())
        buildConfigField("int", "FIELD_TEST_COMPARISON_MIN_SESSIONS_PER_COHORT", fieldTestComparisonMinSessionsPerCohort.toString())
        buildConfigField("int", "FIELD_TEST_COMPARISON_REQUIRE_MATCHING_PRESET", fieldTestComparisonRequireMatchingPreset.toString())
        buildConfigField("String", "FIELD_TEST_COMPARISON_REQUIRED_EVIDENCE", "\"$fieldTestComparisonRequiredEvidence\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Only development-QA-passed visual families enter the main APK. Marker candidates stay test-only
    // until real NAVER base-map evidence passes; design/source masters never enter an Android source set.
    sourceSets {
        getByName("main").assets.srcDir("../design/production/companion")
        getByName("main").assets.srcDir("../design/production/a3/v1")
        getByName("androidTest").assets.srcDir("../design/production")
    }

    testOptions {
        managedDevices {
            localDevices {
                create("pixel2Api30Atd") {
                    device = "Pixel 2"
                    apiLevel = 30
                    systemImageSource = "aosp-atd"
                    testedAbi = "x86"
                }
                // Wide QA host so exact 360/412/600dp A-3 containers fit without clipping.
                create("pixelCApi30Atd") {
                    device = "Pixel C"
                    apiLevel = 30
                    systemImageSource = "aosp-atd"
                    testedAbi = "x86"
                }
            }
        }
    }
}

// Credential verification is intentionally value-blind: CI can prove that injection is
// wired without ever echoing the NCP Key ID to logs or committing it to source control.
tasks.register("verifyNaverMapCredential") {
    group = "verification"
    description = "Fails unless NAVER_MAP_NCP_KEY_ID is supplied through Gradle or environment."
    doLast {
        check(naverMapConfigured) {
            "NAVER_MAP_NCP_KEY_ID is required for credentialed Daily Town builds."
        }
        println("NAVER Maps credential wiring verified for com.dailytown.app")
    }
}

// AGP managed-device setup has emitted a warning that testedAbi was unspecified even while the
// DSL declares x86. Keep the declaration fail-closed and verify the configured DSL model directly
// so CI can distinguish an AGP/setup warning from an accidental Daily Town configuration regression.
tasks.register("verifyManagedDeviceQaConfig") {
    group = "verification"
    description = "Verifies the managed-device QA contract used by replay/NAVER/A-3 tests."
    doLast {
        val expected = listOf(
            "pixel2Api30Atd" to "Pixel 2",
            "pixelCApi30Atd" to "Pixel C",
        )
        val localDevices = android.testOptions.managedDevices.localDevices
        expected.forEach { (name, expectedDevice) ->
            val configured = localDevices.getByName(name)
            check(configured.device == expectedDevice) {
                "$name must use $expectedDevice; found ${configured.device}."
            }
            check(configured.apiLevel == 30) {
                "$name must use API 30; found ${configured.apiLevel}."
            }
            check(configured.systemImageSource == "aosp-atd") {
                "$name must use aosp-atd; found ${configured.systemImageSource}."
            }
            check(configured.testedAbi == "x86") {
                "$name must explicitly use testedAbi=x86; found ${configured.testedAbi}."
            }
        }
        println("Managed-device QA configuration verified: API 30, aosp-atd, testedAbi=x86")
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
    implementation("com.naver.maps:map-sdk:3.23.3")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:core-ktx:1.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    // QA-only SVG adapter. Production runtime uses the semantic production manifest after promotion.
    androidTestImplementation("com.caverock:androidsvg:1.4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
