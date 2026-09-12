package com.dailytown.app.visual

@JvmInline
value class SemanticAssetKey(val value: String) {
    init { require(value.isNotBlank()) }
    override fun toString(): String = value
}

enum class DayPhase { DAWN, MORNING, MIDDAY, AFTERNOON, SUNSET, EVENING, NIGHT }
enum class MarkerFamily { DAY, DARK }
enum class CompanionLightingFamily { LIGHT, WARM_DUSK, DARK }
enum class CompanionExpression(val semantic: String) {
    NEUTRAL("neutral"), HAPPY("happy"), CURIOUS("curious"), SURPRISED("surprised"),
    CLUE_FOUND("clue_found"), RESOLVED("resolved"),
}
enum class AppearanceProfile(val semantic: String) {
    BASE("base"), FAMILIAR("familiar"), TRUSTED("trusted"), BEST_FRIEND("best_friend"),
}
enum class CompanionUsageContext { MAP_AVATAR, HUD_PORTRAIT, ENCOUNTER, RESULT_LARGE, JOURNAL_STAMP }
enum class CompanionMotion(val semantic: String) {
    IDLE_BREATHE("idle_breathe"), CLUE_REACT("clue_react"), RESOLVED_SETTLE("resolved_settle"), WALK("walk"),
}

enum class MarkerSemantic(val key: SemanticAssetKey) {
    ENCOUNTER_HINTED(SemanticAssetKey("marker.encounter.hinted")),
    ENCOUNTER_DISCOVERABLE(SemanticAssetKey("marker.encounter.discoverable")),
    ENCOUNTER_ACTIVE(SemanticAssetKey("marker.encounter.active")),
    ENCOUNTER_SOLVED(SemanticAssetKey("marker.encounter.solved")),
    ENCOUNTER_REVISIT(SemanticAssetKey("marker.encounter.revisit")),
    CLUE(SemanticAssetKey("marker.clue")),
    POI_PARK(SemanticAssetKey("marker.poi.park")),
    POI_CULTURE(SemanticAssetKey("marker.poi.culture")),
    POI_LANDMARK(SemanticAssetKey("marker.poi.landmark")),
    POI_DAILY_LIFE(SemanticAssetKey("marker.poi.daily_life")),
    POI_NATURE(SemanticAssetKey("marker.poi.nature")),
    POI_OTHER(SemanticAssetKey("marker.poi.other")),
}

/** Android/provider-neutral color value. Stored as ARGB 0xAARRGGBB. */
@JvmInline
value class VisualArgb(val value: Long) {
    init { require(value in 0L..0xFFFFFFFFL) }
    fun toAndroidInt(): Int = value.toInt()

    companion object {
        fun rgb(hex: String): VisualArgb {
            val normalized = hex.removePrefix("#")
            require(normalized.length == 6)
            return VisualArgb(0xFF000000L or normalized.toLong(16))
        }
    }
}

data class VisualThemeProfile(
    val phase: DayPhase,
    val markerFamily: MarkerFamily,
    val companionLighting: CompanionLightingFamily,
    val route: VisualArgb,
    val warmLocalPointWeight: Float = 0f,
    val coolAmbientWeight: Float = 0f,
    val eveningProgress: Float? = null,
)
