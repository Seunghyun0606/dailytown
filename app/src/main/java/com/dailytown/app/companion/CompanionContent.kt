package com.dailytown.app.companion

import com.dailytown.app.mystery.MysteryMechanic
import com.dailytown.app.visual.AppearanceProfile

/**
 * Relationship stage shown in Companion and used for Moru's affinity appearance layer.
 * The 20/50 boundaries are the ones the Companion screen already used for its labels; they are
 * kept in this single place so the pending product decision on affinity cut-offs (and a future
 * BEST_FRIEND stage) changes one table only.
 */
enum class RelationshipTier(val label: String, val minBond: Int, val appearance: AppearanceProfile) {
    NEW("새로운 사이", 0, AppearanceProfile.BASE),
    FAMILIAR("익숙한 사이", 20, AppearanceProfile.FAMILIAR),
    CLOSE("가까운 사이", 50, AppearanceProfile.TRUSTED),
}

object CompanionRelationship {
    fun tier(bond: Int): RelationshipTier =
        RelationshipTier.entries.last { bond >= it.minBond }

    /** Progress inside the current stage as (current, needed) toward the next stage, or null at the top. */
    fun progressToNext(bond: Int): Pair<Int, Int>? {
        val current = tier(bond)
        val next = RelationshipTier.entries.getOrNull(current.ordinal + 1) ?: return null
        return (bond - current.minBond).coerceAtLeast(0) to (next.minBond - current.minBond)
    }
}

/**
 * One keepsake per mystery mechanic, unlocked by the `mechanic:<NAME>` companion memory that a
 * resolution already writes. Keepsakes are shared memories, not an item economy (no trading or
 * currency), which is what the approved scenario asks of the "선물·기념품" entry point.
 */
object KeepsakeCatalog {
    data class Keepsake(val mechanic: MysteryMechanic, val name: String, val note: String)

    val all: List<Keepsake> = listOf(
        Keepsake(MysteryMechanic.TRACE_CHAIN, "잎자국이 남은 메모", "누군가 다음 사람에게 남긴 작은 힌트"),
        Keepsake(MysteryMechanic.SOUND_PATTERN, "작은 깃털", "박자를 맞추던 소리 곁에 떨어져 있던 것"),
        Keepsake(MysteryMechanic.TIME_LAYER, "낡은 열쇠", "오래된 표식처럼 시간이 묻어 있는 열쇠"),
        Keepsake(MysteryMechanic.SYMBOL_MATCH, "은행잎 책갈피", "반복되던 무늬를 닮은 잎 한 장"),
        Keepsake(MysteryMechanic.LOST_OBJECT, "도토리", "물건 주인을 기다리며 모루가 주운 도토리"),
        Keepsake(MysteryMechanic.PHOTO_ANGLE, "작은 지도 조각", "시점을 바꾸면 보이던 길을 그려 둔 조각"),
        Keepsake(MysteryMechanic.LOCAL_MEMORY, "말린 꽃", "동네 이야기가 쌓인 자리에 피어 있던 꽃"),
        Keepsake(MysteryMechanic.COMPANION_SENSE, "새싹 리본", "모루가 처음 무언가를 느꼈던 날의 표시"),
    )

    init {
        check(all.map { it.mechanic }.toSet() == MysteryMechanic.entries.toSet()) { "one keepsake per mechanic" }
    }

    fun memoryKey(mechanic: MysteryMechanic): String = "mechanic:${mechanic.name}"

    fun unlocked(memoryKeys: Set<String>): List<Keepsake> = all.filter { memoryKey(it.mechanic) in memoryKeys }
}

data class DialogueReply(val label: String, val moruResponse: String)

data class DialogueTurn(val topicId: String, val moruLine: String, val replies: List<DialogueReply>)

data class DialogueContext(
    val companionName: String,
    val bond: Int,
    val rememberedPlace: String?,
    val discoveredToday: Int,
    val resolvedToday: Int,
    val keepsakeCount: Int,
    val totalMemories: Int,
)

/**
 * Authored, rule-based conversation (scenario v2: "authored scenario rules and semantic memories
 * rather than unconstrained chatbot behavior"). Topics are chosen from what actually happened;
 * [rotation] lets the player ask again for a different applicable topic. No network, no free text.
 */
object CompanionDialogue {
    fun topics(context: DialogueContext): List<DialogueTurn> {
        val name = context.companionName
        val list = mutableListOf<DialogueTurn>()
        if (context.resolvedToday > 0) {
            list += DialogueTurn(
                "resolved_today",
                "오늘 풀었던 이야기, 아직도 조금 두근거려.",
                listOf(
                    DialogueReply("나도 재밌었어", "다음에도 같이 풀자. 너랑 하면 더 잘 보이거든."),
                    DialogueReply("어떤 점이?", "누군가 남긴 흔적을 우리가 이어받은 느낌이 들었어."),
                ),
            )
        }
        context.rememberedPlace?.let { place ->
            list += DialogueTurn(
                "remembered_place",
                "$place 기억나? 그때랑 지금은 뭐가 달라졌을까.",
                listOf(
                    DialogueReply("다시 가 보자", "좋아. 같은 곳도 다시 보면 새로운 게 있을 거야."),
                    DialogueReply("거기 좋았어", "응, 나도. 그래서 기억에 남겨 뒀어."),
                ),
            )
        }
        if (context.discoveredToday > 0 && context.resolvedToday == 0) {
            list += DialogueTurn(
                "unfinished_today",
                "아까 발견한 곳, 아직 다 살펴보지 못했지?",
                listOf(
                    DialogueReply("다시 가 볼까", "응! 신호가 남아 있으면 내가 먼저 알려줄게."),
                    DialogueReply("다음에 하자", "괜찮아. 동네는 어디 가지 않으니까."),
                ),
            )
        }
        if (context.keepsakeCount > 0) {
            list += DialogueTurn(
                "keepsakes",
                "우리가 모은 기념품이 벌써 ${context.keepsakeCount}개야. 가끔 꺼내 보고 있어.",
                listOf(
                    DialogueReply("제일 좋은 건?", "음… 처음 같이 찾은 게 제일 좋아."),
                    DialogueReply("더 모으자", "천천히 모으자. 하나하나 이야기가 있으니까."),
                ),
            )
        }
        val tier = CompanionRelationship.tier(context.bond)
        list += when (tier) {
            RelationshipTier.NEW -> DialogueTurn(
                "getting_to_know",
                "아직 서로 잘 모르지만, 같이 걷는 건 벌써 좋아.",
                listOf(
                    DialogueReply("나도 그래", "다행이다. 오늘은 어디로 가 볼까?"),
                    DialogueReply("너는 어디서 왔어?", "새싹이 돋는 골목에서 왔어. 그래서 작은 걸 잘 찾아."),
                ),
            )
            RelationshipTier.FAMILIAR -> DialogueTurn(
                "familiar",
                "이제 네 걸음 속도에 조금 익숙해졌어.",
                listOf(
                    DialogueReply("천천히 걸을게", "고마워. 천천히 걸으면 더 많이 보여."),
                    DialogueReply("오늘은 멀리 가자", "좋아, 대신 쉬어 가면서!"),
                ),
            )
            RelationshipTier.CLOSE -> DialogueTurn(
                "close",
                "$name 혼자였으면 몰랐을 장소가 많아. 고마워.",
                listOf(
                    DialogueReply("나도 고마워", "앞으로도 같이 걸어 줘."),
                    DialogueReply("다음엔 어디로?", "아직 가보지 않은 골목이 남아 있어. 거기로 가자."),
                ),
            )
        }
        list += DialogueTurn(
            "invite_walk",
            if (context.totalMemories == 0) "오늘은 가까운 골목부터 천천히 걸어볼까?" else "오늘도 조금만 걸어볼래? 짧아도 괜찮아.",
            listOf(
                DialogueReply("좋아, 가자", "탐험 탭에서 시작하면 내가 신호를 찾아볼게."),
                DialogueReply("조금 이따가", "응, 기다릴게. 나갈 때 불러 줘."),
            ),
        )
        return list
    }

    fun turn(context: DialogueContext, rotation: Int): DialogueTurn {
        val options = topics(context)
        return options[Math.floorMod(rotation, options.size)]
    }
}
