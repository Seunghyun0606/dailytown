package com.dailytown.app.companion

import com.dailytown.app.mystery.MysteryMechanic
import com.dailytown.app.visual.AppearanceProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CompanionContentTest {
    @Test
    fun relationshipTiersKeepTheExistingBoundariesAndMapToAffinityLayers() {
        assertEquals(RelationshipTier.NEW, CompanionRelationship.tier(0))
        assertEquals(RelationshipTier.NEW, CompanionRelationship.tier(19))
        assertEquals(RelationshipTier.FAMILIAR, CompanionRelationship.tier(20))
        assertEquals(RelationshipTier.CLOSE, CompanionRelationship.tier(50))
        assertEquals(AppearanceProfile.BASE, RelationshipTier.NEW.appearance)
        assertEquals(AppearanceProfile.FAMILIAR, RelationshipTier.FAMILIAR.appearance)
        assertEquals(AppearanceProfile.TRUSTED, RelationshipTier.CLOSE.appearance)
        assertEquals(12 to 20, CompanionRelationship.progressToNext(12))
        assertEquals(5 to 30, CompanionRelationship.progressToNext(25))
        assertNull(CompanionRelationship.progressToNext(80))
    }

    @Test
    fun keepsakesUnlockFromTheExistingResolvedMechanicMemories() {
        assertEquals(MysteryMechanic.entries.size, KeepsakeCatalog.all.size)
        val unlocked = KeepsakeCatalog.unlocked(setOf("mechanic:TRACE_CHAIN", "poi:abc", "mechanic:TIME_LAYER"))
        assertEquals(listOf(MysteryMechanic.TRACE_CHAIN, MysteryMechanic.TIME_LAYER), unlocked.map { it.mechanic })
        assertTrue(KeepsakeCatalog.unlocked(emptySet()).isEmpty())
    }

    private fun context(
        bond: Int = 12,
        place: String? = null,
        discovered: Int = 0,
        resolved: Int = 0,
        keepsakes: Int = 0,
        memories: Int = 0,
    ) = DialogueContext("모루", bond, place, discovered, resolved, keepsakes, memories)

    @Test
    fun dialogueOnlyOffersTopicsBackedByRealProgress() {
        val fresh = CompanionDialogue.topics(context()).map { it.topicId }
        assertEquals(listOf("getting_to_know", "invite_walk"), fresh)

        val busy = CompanionDialogue.topics(
            context(bond = 55, place = "덕수궁 돌담길", discovered = 2, resolved = 1, keepsakes = 2, memories = 4),
        ).map { it.topicId }
        assertEquals(listOf("resolved_today", "remembered_place", "keepsakes", "close", "invite_walk"), busy)

        val unfinished = CompanionDialogue.topics(context(discovered = 1)).map { it.topicId }
        assertTrue("unfinished" in unfinished.first())
    }

    @Test
    fun everyTurnHasTwoAuthoredRepliesAndRotationCyclesTopics() {
        val ctx = context(place = "서울시청", memories = 1)
        val topics = CompanionDialogue.topics(ctx)
        topics.forEach { turn ->
            assertEquals(2, turn.replies.size)
            assertTrue(turn.moruLine.isNotBlank())
            turn.replies.forEach { assertTrue(it.label.isNotBlank() && it.moruResponse.isNotBlank()) }
        }
        assertEquals(topics[0], CompanionDialogue.turn(ctx, 0))
        assertEquals(topics[1], CompanionDialogue.turn(ctx, 1))
        assertEquals(topics[0], CompanionDialogue.turn(ctx, topics.size))
        assertTrue(CompanionDialogue.turn(ctx, 0).moruLine.contains("서울시청"))
    }
}
