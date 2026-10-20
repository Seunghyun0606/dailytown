package com.dailytown.app.mystery

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScenarioContentPackTest {
    @Test
    fun packValidatesWithFullMechanicCoverageAndOneApprovedScenario() {
        assertEquals(emptyList<String>(), ScenarioContentPack.validate())
        MysteryMechanic.entries.forEach { assertEquals(it, ScenarioContentPack.entry(it).mechanic) }
        assertEquals(
            ScenarioCopyStatus.AUTHORED_APPROVED,
            ScenarioContentPack.entry(MysteryMechanic.TRACE_CHAIN).status,
        )
        assertEquals(
            7,
            MysteryMechanic.entries.count { ScenarioContentPack.entry(it).status == ScenarioCopyStatus.ENGINEERING_DRAFT },
        )
    }

    @Test
    fun approvedOldGinkgoCopyIsPreservedVerbatim() {
        val copy = ScenarioContentPack.copy(MysteryMechanic.TRACE_CHAIN, "아무 장소", isRevisit = false)
        assertEquals("오래된 가로수의 메모", copy.title)
        assertEquals("저쪽이 조금 신경 쓰이는데?", copy.detectMoruLine)
        assertEquals("누군가 다음 사람에게 작은 힌트를 남긴 것 같아. 우리도 기억해두자.", copy.resolveMoruLine)
    }

    @Test
    fun draftsUseTheRealPlaceNameAndNeverLeakTokens() {
        MysteryMechanic.entries.filter { it != MysteryMechanic.TRACE_CHAIN }.forEach { mechanic ->
            listOf(false, true).forEach { revisit ->
                val copy = ScenarioContentPack.copy(mechanic, "서울시청", revisit)
                val all = listOf(copy.title, copy.hint, copy.premise, copy.clueLabel, copy.resolution, copy.resolveMoruLine)
                assertTrue("$mechanic should mention the place", (copy.title + copy.hint).contains("서울시청"))
                all.forEach { assertFalse("$mechanic leaked a token: $it", it.contains('{')) }
            }
        }
    }

    @Test
    fun revisitCopyDiffersFromFirstVisitSoRepeatAreasDoNotReadIdentically() {
        MysteryMechanic.entries.forEach { mechanic ->
            val first = ScenarioContentPack.copy(mechanic, "덕수궁 돌담길", isRevisit = false)
            val again = ScenarioContentPack.copy(mechanic, "덕수궁 돌담길", isRevisit = true)
            assertTrue("$mechanic revisit hint", first.hint != again.hint)
            assertTrue("$mechanic revisit resolution", first.resolution != again.resolution)
        }
    }

    @Test
    fun blankPlaceNameFallsBackToNeutralWording() {
        val copy = ScenarioContentPack.copy(MysteryMechanic.SOUND_PATTERN, "  ", isRevisit = false)
        assertTrue(copy.hint.startsWith("이 근처"))
    }
}
