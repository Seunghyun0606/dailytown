package com.dailytown.app.visualqa

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.test.core.graphics.writeToTestStorage
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dailytown.app.visual.AppearanceProfile
import com.dailytown.app.visual.CompanionExpression
import com.dailytown.app.visual.CompanionLightingFamily
import com.dailytown.app.visual.MarkerFamily
import com.dailytown.app.visual.MarkerSemantic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VisualCandidateQaTest {
    private val catalog by lazy {
        CandidateAssetCatalog(InstrumentationRegistry.getInstrumentation().context.assets)
    }

    @Test
    fun companionExpressionLightingMatrixRendersAndAligns() {
        val required = setOf(
            "moru.neutral.LIGHT",
            "moru.clue_found.WARM_DUSK",
            "moru.resolved.DARK",
            "luca.neutral.LIGHT",
            "luca.clue_found.WARM_DUSK",
            "luca.resolved.DARK",
        )
        listOf("moru", "luca").forEach { companionId ->
            val transforms = CompanionLightingFamily.entries.map {
                CandidateSvgRenderer.lightingTransform(catalog, companionId, it)
            }.toSet()
            assertEquals("lighting/body anchor transform must stay fixed", 1, transforms.size)

            val contact = Bitmap.createBitmap(48 * CompanionExpression.entries.size, 48 * CompanionLightingFamily.entries.size, Bitmap.Config.ARGB_8888)
            val contactCanvas = Canvas(contact)
            CompanionLightingFamily.entries.forEachIndexed { row, lighting ->
                CompanionExpression.entries.forEachIndexed { column, expression ->
                    val bitmap = CandidateSvgRenderer.renderCompanion(catalog, companionId, expression, lighting, targetPx = 48)
                    val (bodyBounds, expressionBounds) = CandidateSvgRenderer.renderCompanionLayerBounds(catalog, companionId, expression, lighting, 96)
                    assertTrue("expression must stay within canonical lighting/body bounds", bodyBounds.contains(expressionBounds))
                    contactCanvas.drawBitmap(bitmap, (column * 48).toFloat(), (row * 48).toFloat(), null)
                    val key = "$companionId.${expression.semantic}.${lighting.name}"
                    if (key in required) bitmap.writeToTestStorage("visual/companion/$key.48dp")
                }
            }
            contact.writeToTestStorage("visual/companion/$companionId.expression-lighting.48dp.contact-sheet")
            CandidateSvgRenderer.renderCompanion(
                catalog,
                companionId,
                CompanionExpression.NEUTRAL,
                CompanionLightingFamily.LIGHT,
                targetPx = 32,
            ).writeToTestStorage("visual/companion/$companionId.neutral.LIGHT.32dp")
        }
    }

    @Test
    fun moruAffinityProfilesComposeWithoutReplacingExpressionOrLighting() {
        val strip = Bitmap.createBitmap(64 * AppearanceProfile.entries.size, 64, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(strip)
        AppearanceProfile.entries.forEachIndexed { index, profile ->
            val bitmap = CandidateSvgRenderer.renderCompanion(
                catalog = catalog,
                companionId = "moru",
                expression = CompanionExpression.CLUE_FOUND,
                lighting = CompanionLightingFamily.WARM_DUSK,
                appearance = profile,
                targetPx = 64,
            )
            canvas.drawBitmap(bitmap, (index * 64).toFloat(), 0f, null)
        }
        strip.writeToTestStorage("visual/companion/moru.affinity.clue_found.WARM_DUSK.contact-sheet")
    }

    @Test
    fun markerDayDarkCandidatesRasterizeAtCanonicalAnchor() {
        MarkerFamily.entries.forEach { family ->
            val sheet = Bitmap.createBitmap(48 * 6, 64 * 2, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(sheet)
            MarkerSemantic.entries.forEachIndexed { index, semantic ->
                val bitmap = CandidateSvgRenderer.renderMarker(catalog, semantic.key.value, family)
                canvas.drawBitmap(bitmap, ((index % 6) * 48).toFloat(), ((index / 6) * 64).toFloat(), null)
            }
            sheet.writeToTestStorage("visual/marker/${family.name.lowercase()}.12-markers.contact-sheet")
        }
    }

    @Test
    fun candidateCatalogContainsExactPreparedCounts() {
        val entries = catalog.allEntries()
        assertEquals(65, entries.size)
        assertEquals(24, entries.count { it.family == "DAY" || it.family == "DARK" })
        assertEquals(32, entries.count { it.semanticKey.startsWith("companion.") || it.semanticKey.startsWith("lighting.") || it.semanticKey.startsWith("appearance.") })
        assertEquals(9, entries.count { it.semanticKey.startsWith("surface.") || it.semanticKey.startsWith("sticker.") || it.semanticKey.startsWith("card.") || it.semanticKey.startsWith("stamp.") || it.semanticKey.startsWith("collection.") })
    }
}
