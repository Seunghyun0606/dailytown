package com.dailytown.app.visualqa

import android.content.res.AssetManager
import com.dailytown.app.visual.CompanionLightingFamily
import com.dailytown.app.visual.EveningDebugState
import com.dailytown.app.visual.EveningVisualInterpolator
import com.dailytown.app.visual.MapOverlayQaMatrix
import com.dailytown.app.visual.MapQaComplexity
import com.dailytown.app.visual.MapQaMotionMode
import com.dailytown.app.visual.MapQaTimeAnchor
import com.dailytown.app.visual.MarkerFamily
import com.dailytown.app.visual.VisualArgb
import org.json.JSONArray
import org.json.JSONObject

internal data class EveningQaCheckpoint(
    val id: String,
    val progress: Float,
    val hudDeepNavyMix: Float,
)

/** Reads approved design handoff files as test-only source-of-truth inputs. */
internal class MapOverlayQaSourceCatalog(
    private val assets: AssetManager,
) {
    private val matrix by lazy { readJson("map-overlay-qa-matrix.v1.json") }
    private val evening by lazy { readJson("evening-interpolation-tokens.v1.json") }

    fun verifyApprovedSourcesAndRuntimeContract(): List<EveningQaCheckpoint> {
        check(matrix.getString("status") == "approved_source")
        check(evening.getString("status") == "approved_source")
        check(evening.getString("strategy") == "EV-1_interpolation_first")
        verifyBaselineMatrix()
        verifyRequiredStack()
        return verifyEveningCheckpoints()
    }

    private fun verifyBaselineMatrix() {
        val dimensions = matrix.getJSONObject("dimensions")
        val sourceTimes = dimensions.getJSONArray("time_anchor").strings()
        val sourceComplexities = dimensions.getJSONArray("map_complexity").strings()
        val sourceMotionModes = dimensions.getJSONArray("motion_mode").strings()

        check(sourceTimes == MapQaTimeAnchor.values().map { it.semantic })
        check(sourceComplexities == MapQaComplexity.values().map { it.semantic })
        check(sourceMotionModes == MapQaMotionMode.values().map { it.semantic })
        check(matrix.getInt("expected_capture_count") == MapOverlayQaMatrix.EXPECTED_BASELINE_CAPTURE_COUNT)
        check(MapOverlayQaMatrix.baselineCases.size == MapOverlayQaMatrix.EXPECTED_BASELINE_CAPTURE_COUNT)
    }

    private fun verifyRequiredStack() {
        val source = matrix.getJSONArray("required_stack").strings().toSet()
        val supported = setOf(
            "provider_map",
            "companion_map_avatar",
            "encounter_marker",
            "poi_marker",
            "route_following",
            "active_halo",
            "discovery_effect_or_static_fallback",
            "hud",
        )
        check(source == supported) {
            "Map overlay QA required stack changed; update the Android QA renderer explicitly."
        }
    }

    private fun verifyEveningCheckpoints(): List<EveningQaCheckpoint> {
        val markerPolicy = evening.getJSONObject("marker_policy")
        check(markerPolicy.getBoolean("new_evening_marker_family_forbidden"))
        check(markerPolicy.getJSONArray("families").strings() == listOf("DAY", "DARK"))
        check(markerPolicy.getDouble("luminance_threshold").toFloat() == EveningVisualInterpolator.MAP_LUMINANCE_THRESHOLD)

        val checkpoints = evening.getJSONObject("checkpoints")
        val results = EveningDebugState.values().map { state ->
            val source = checkpoints.getJSONObject(state.name)
            val progress = source.getDouble("progress").toFloat()
            check(progress == state.progress)
            val expectedRoute = VisualArgb.rgb(source.getString("route"))
            val runtime = EveningVisualInterpolator.profile(progress)
            check(runtime.route == expectedRoute) { "${state.name} EV-1 route token drifted" }

            when (state) {
                EveningDebugState.E0, EveningDebugState.E1 -> {
                    check(runtime.markerFamily == MarkerFamily.DAY)
                    check(runtime.companionLighting == CompanionLightingFamily.WARM_DUSK)
                }
                EveningDebugState.E2 -> {
                    check(source.getString("marker_family_fallback") == "AUTO_BY_MAP_LUMINANCE")
                    check(runtime.companionLighting == CompanionLightingFamily.WARM_DUSK)
                }
                EveningDebugState.E3, EveningDebugState.E4 -> {
                    check(runtime.markerFamily == MarkerFamily.DARK)
                    check(runtime.companionLighting == CompanionLightingFamily.DARK)
                }
            }

            val surface = source.getJSONObject("hud_surface")
            val deepNavyMix = when {
                surface.has("deep_navy_mix") -> surface.getDouble("deep_navy_mix").toFloat()
                surface.has("ivory_mix") -> 1f - surface.getDouble("ivory_mix").toFloat()
                else -> error("${state.name} HUD surface has no interpolation weight")
            }
            check(deepNavyMix in 0f..1f)
            EveningQaCheckpoint(state.name, progress, deepNavyMix)
        }
        check(results.map { it.progress } == listOf(0f, .25f, .5f, .75f, 1f))
        check(evening.getJSONObject("qa").getString("mandatory_checkpoint") == "E2")
        return results
    }

    private fun readJson(path: String): JSONObject =
        assets.open(path).bufferedReader(Charsets.UTF_8).use { JSONObject(it.readText()) }
}

private fun JSONArray.strings(): List<String> =
    (0 until length()).map { getString(it) }
