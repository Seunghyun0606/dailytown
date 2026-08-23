package com.dailytown.app.mystery

import java.time.LocalDate
import java.time.LocalTime

enum class TimeBand { DAWN, DAY, EVENING, NIGHT }

data class EncounterContext(
    val dayKey: String,
    val timeBand: TimeBand,
    val companionBond: Int,
    val memoryKeys: Set<String> = emptySet(),
)

object EncounterContextFactory {
    fun create(
        date: LocalDate = LocalDate.now(),
        time: LocalTime = LocalTime.now(),
        companionBond: Int,
        memoryKeys: Set<String> = emptySet(),
    ): EncounterContext = EncounterContext(
        dayKey = date.toString(),
        timeBand = timeBand(time.hour),
        companionBond = companionBond,
        memoryKeys = memoryKeys,
    )

    fun timeBand(hour: Int): TimeBand = when (hour.coerceIn(0, 23)) {
        in 5..8 -> TimeBand.DAWN
        in 9..16 -> TimeBand.DAY
        in 17..20 -> TimeBand.EVENING
        else -> TimeBand.NIGHT
    }
}

class RareEncounterPolicy {
    fun isEligible(template: MysteryTemplate, poiId: String, context: EncounterContext): Boolean {
        val threshold = when (template.rarity) {
            EncounterRarity.COMMON -> return true
            EncounterRarity.UNCOMMON -> 32
            EncounterRarity.RARE -> (7 + context.companionBond.coerceIn(0, 100) / 10).coerceAtMost(17)
        }
        return bucket("${context.dayKey}:$poiId:${template.id}") < threshold
    }

    internal fun bucket(seed: String): Int {
        var value = 17L
        seed.forEach { char -> value = (value * 37L + char.code) and 0x7fffffffL }
        return (value % 100L).toInt()
    }
}
