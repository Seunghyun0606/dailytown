package com.dailytown.app.ui.presentation

import com.dailytown.app.domain.GeoPoint
import com.dailytown.app.mystery.EncounterContext
import com.dailytown.app.mystery.EncounterPhase
import com.dailytown.app.mystery.EncounterSelection
import com.dailytown.app.mystery.MysteryEncounter
import com.dailytown.app.mystery.MysteryMechanic
import com.dailytown.app.mystery.MysteryTemplate
import com.dailytown.app.mystery.TimeBand
import com.dailytown.app.poi.Poi
import com.dailytown.app.poi.PoiCategory
import com.dailytown.app.visual.OldGinkgoVisualAssets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExploreEncounterPresentationTest {
    @Test
    fun hiddenOrMissingEncounterMapsToPrepareWithoutScenarioArt() {
        val missing = ExploreEncounterPresentationMapper.map(null, null)
        val hidden = ExploreEncounterPresentationMapper.map(selection(EncounterPhase.HIDDEN), 170)

        assertEquals(ExploreExperienceStep.PREPARE, missing.step)
        assertEquals(ExploreExperienceStep.PREPARE, hidden.step)
        assertNull(missing.placeAssetKey)
        assertNull(hidden.noteAssetKey)
    }

    @Test
    fun hintedEncounterUsesUiOnlyNearBoundaryWithoutChangingDomainPhaseOrRevealingArt() {
        val hinted = selection(EncounterPhase.HINTED)

        val detect = ExploreEncounterPresentationMapper.map(hinted, 121)
        val approach = ExploreEncounterPresentationMapper.map(hinted, 120)
        val closeApproach = ExploreEncounterPresentationMapper.map(hinted, 61)

        assertEquals(ExploreExperienceStep.DETECT, detect.step)
        assertEquals(ExploreExperienceStep.APPROACH, approach.step)
        assertEquals(ExploreExperienceStep.APPROACH, closeApproach.step)
        assertEquals(EncounterPhase.HINTED, hinted.encounter.phase)
        assertEquals("알 수 없는 신호", detect.signalLabel)
        assertTrue(detect.hint!!.contains("오래된 나무"))
        assertNull(detect.placeAssetKey)
        assertNull(detect.noteAssetKey)
        assertNull(detect.clueAssetKey)
        assertNull(detect.memoryAssetKey)
    }

    @Test
    fun discoveredEncounterBindsApprovedOldGinkgoPlaceAndClueSemantics() {
        val reveal = ExploreEncounterPresentationMapper.map(selection(EncounterPhase.DISCOVERED), 58)
        val investigation = ExploreEncounterPresentationMapper.map(
            selection(EncounterPhase.DISCOVERED, clueIds = setOf("enc-0:poi:trace-chain:clue-1")),
            40,
        )

        assertEquals(ExploreExperienceStep.DISCOVER, reveal.step)
        assertEquals(ExploreExperienceStep.INVESTIGATE, investigation.step)
        assertEquals("leaf_mark_note", reveal.clueSemanticKey)
        assertEquals("잎자국이 남은 메모", reveal.clueLabel)
        assertEquals(OldGinkgoVisualAssets.PlaceMain, reveal.placeAssetKey)
        assertEquals(OldGinkgoVisualAssets.FoldedNote, reveal.noteAssetKey)
        assertEquals(OldGinkgoVisualAssets.GinkgoLeaf, reveal.clueAssetKey)
        assertNull(reveal.memoryAssetKey)
        assertEquals(reveal.placeAssetKey, investigation.placeAssetKey)
        assertEquals(reveal.noteAssetKey, investigation.noteAssetKey)
        assertEquals(reveal.clueAssetKey, investigation.clueAssetKey)
    }

    @Test
    fun resolvedEncounterBindsKeepsakeWithoutChangingCompletionSemantics() {
        val result = ExploreEncounterPresentationMapper.map(selection(EncounterPhase.RESOLVED), 30)

        assertEquals(ExploreExperienceStep.RECORD, result.step)
        assertEquals("오래된 가로수의 메모", result.title)
        assertTrue(result.moruLine!!.contains("우리도 기억해두자"))
        assertEquals(OldGinkgoVisualAssets.Keepsake, result.memoryAssetKey)
    }

    @Test
    fun revisitUsesMemoryAwareCopyWithoutInventingNewProgressState() {
        val result = ExploreEncounterPresentationMapper.map(
            selection(EncounterPhase.HINTED, isRevisit = true),
            150,
        )

        assertTrue(result.isRevisit)
        assertTrue(result.hint!!.contains("다시"))
        assertNull(result.clueSemanticKey)
        assertNull(result.memoryAssetKey)
    }

    private fun selection(
        phase: EncounterPhase,
        clueIds: Set<String> = emptySet(),
        isRevisit: Boolean = false,
    ): EncounterSelection {
        val template = MysteryTemplate(
            id = "trace-chain",
            mechanic = MysteryMechanic.TRACE_CHAIN,
            requiredClues = 2,
        )
        return EncounterSelection(
            poi = Poi(
                id = "poi-tree",
                name = "테스트 가로수",
                position = GeoPoint(37.5665, 126.9780),
                districtKey = "test",
                category = PoiCategory.STREET,
            ),
            template = template,
            encounter = MysteryEncounter(
                id = "enc-0:poi-tree:trace-chain",
                templateId = template.id,
                poiId = "poi-tree",
                phase = phase,
                clueIds = clueIds,
            ),
            context = EncounterContext(
                dayKey = "2026-09-11",
                timeBand = TimeBand.DAY,
                companionBond = 12,
            ),
            isRevisit = isRevisit,
        )
    }
}
