package com.dailytown.app

/** Release capabilities selected by the Android source set. QA-only surfaces stay disabled. */
internal object BuildVariantPolicy {
    const val allowFixturePoiFallback: Boolean = false
    const val showFixturePoiMarkers: Boolean = false
    const val exposeFieldTestTools: Boolean = false
    const val allowDirectPoiProvider: Boolean = false
}
