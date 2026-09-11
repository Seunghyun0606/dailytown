package com.dailytown.app

/** Debug/internal-only capabilities selected by the Android source set. */
internal object BuildVariantPolicy {
    const val allowFixturePoiFallback: Boolean = true
    const val showFixturePoiMarkers: Boolean = true
    const val exposeFieldTestTools: Boolean = true
}
