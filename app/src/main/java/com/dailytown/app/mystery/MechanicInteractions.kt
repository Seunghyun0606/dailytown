package com.dailytown.app.mystery

/**
 * Mechanic-specific investigation step (scenario v2 §Investigate: ~1–3 actions, 15–45 s, no precision
 * controls). Solving one step yields the next clue through the existing idempotent clue reducer; a
 * wrong answer never fails the encounter, it only asks to try again.
 */
enum class InteractionKind { CHOICE, SEQUENCE }

data class MechanicInteraction(
    val mechanic: MysteryMechanic,
    val kind: InteractionKind,
    val prompt: String,
    /** Options in display order. */
    val options: List<String>,
    /** Display-order indices the player must pick, in order (single element for CHOICE). */
    val answer: List<Int>,
    val successLine: String,
    val retryLine: String,
) {
    init {
        require(options.size >= 2)
        require(answer.isNotEmpty() && answer.all { it in options.indices })
        if (kind == InteractionKind.CHOICE) require(answer.size == 1)
    }
}

sealed interface InteractionProgress {
    data class Partial(val picks: List<Int>) : InteractionProgress
    data object Solved : InteractionProgress
    data object Wrong : InteractionProgress
}

object MechanicInteractions {
    private data class Authored(
        val kind: InteractionKind,
        val prompt: String,
        /** Canonical options; the first [answerLength] entries of [canonicalAnswer] index into this list. */
        val options: List<String>,
        val canonicalAnswer: List<Int>,
        val success: String,
        val retry: String,
        val shuffle: Boolean = true,
    )

    private val authored: Map<MysteryMechanic, List<Authored>> = mapOf(
        MysteryMechanic.TRACE_CHAIN to listOf(
            Authored(
                InteractionKind.SEQUENCE,
                "메모에 적힌 순서대로 흔적을 따라가 보자. 잎자국 → 돌계단 → 가로등",
                listOf("잎자국이 남은 벽", "돌계단 모서리", "가로등 아래"),
                listOf(0, 1, 2),
                "흔적이 하나로 이어졌어!",
                "순서가 조금 달랐나 봐. 메모를 다시 보자.",
            ),
            Authored(
                InteractionKind.SEQUENCE,
                "두 번째 메모의 순서야. 나무 → 벤치 → 담장 모퉁이",
                listOf("나무 밑동", "낡은 벤치", "담장 모퉁이"),
                listOf(0, 1, 2),
                "다음 사람을 위한 길이었어.",
                "음, 순서를 다시 따라가 보자.",
            ),
        ),
        MysteryMechanic.SOUND_PATTERN to listOf(
            Authored(
                InteractionKind.SEQUENCE,
                "모루가 들은 박자를 따라 쳐 보자. 짧게 · 짧게 · 길게",
                listOf("짧게 ♪", "길게 ♩"),
                listOf(0, 0, 1),
                "맞아, 그 박자야!",
                "박자가 살짝 어긋났어. 다시 들어 보자.",
                shuffle = false,
            ),
            Authored(
                InteractionKind.SEQUENCE,
                "이번엔 조금 달라. 길게 · 짧게 · 길게",
                listOf("짧게 ♪", "길게 ♩"),
                listOf(1, 0, 1),
                "소리가 대답하는 것 같아.",
                "한 번만 더 해 보자.",
                shuffle = false,
            ),
        ),
        MysteryMechanic.TIME_LAYER to listOf(
            Authored(
                InteractionKind.CHOICE,
                "셋 중에서 가장 오래된 흔적은 뭘까?",
                listOf("닳아서 흐려진 숫자 표식", "새로 칠한 표지판", "반짝이는 우편함"),
                listOf(0),
                "숫자가 이 길의 나이를 알려 줘.",
                "그건 꽤 새것 같아. 다른 걸 보자.",
            ),
            Authored(
                InteractionKind.CHOICE,
                "예전 길의 경계는 어디였을까?",
                listOf("이끼 낀 경계석", "매끈한 보도블록", "새 가로등"),
                listOf(0),
                "옛 길이 여기서 끝났었구나.",
                "흠, 조금 더 오래돼 보이는 걸 찾아 보자.",
            ),
        ),
        MysteryMechanic.SYMBOL_MATCH to listOf(
            Authored(
                InteractionKind.CHOICE,
                "아까 벽에서 본 무늬는 세 갈래 잎이었어. 같은 무늬는?",
                listOf("세 갈래 잎 무늬", "둥근 잎 무늬", "두 갈래 잎 무늬"),
                listOf(0),
                "같은 무늬가 이어져 있어!",
                "비슷하지만 달라. 잎 갈래를 세어 보자.",
            ),
            Authored(
                InteractionKind.CHOICE,
                "마지막 무늬는 가운데 점이 있었어. 어느 쪽일까?",
                listOf("가운데 점이 있는 잎", "테두리만 있는 잎", "줄무늬 잎"),
                listOf(0),
                "무늬가 모두 같은 곳을 가리켜.",
                "점을 다시 찾아 보자.",
            ),
        ),
        MysteryMechanic.LOST_OBJECT to listOf(
            Authored(
                InteractionKind.CHOICE,
                "주인은 어디서 이걸 떨어뜨렸을까? 흔적을 따라가 보자.",
                listOf("벤치 밑 작은 발자국", "나무 위 새둥지", "분수대 물속"),
                listOf(0),
                "여기서부터 걸어왔나 봐.",
                "거긴 아닌 것 같아. 땅 쪽을 보자.",
            ),
            Authored(
                InteractionKind.CHOICE,
                "주인이 다시 찾으러 올 때 보이게 두려면?",
                listOf("눈에 잘 띄는 벤치 위", "덤불 속 깊이", "가방에 넣어 가기"),
                listOf(0),
                "주인이 꼭 찾았으면 좋겠다.",
                "음, 주인이 찾기 어려울 것 같아.",
            ),
        ),
        MysteryMechanic.PHOTO_ANGLE to listOf(
            Authored(
                InteractionKind.CHOICE,
                "어디에 서면 숨은 모양이 보일까?",
                listOf("계단 맨 위", "바로 코앞", "등을 돌린 자리"),
                listOf(0),
                "위에서 보니 모양이 보여!",
                "여기선 잘 안 보여. 자리를 바꿔 보자.",
            ),
            Authored(
                InteractionKind.CHOICE,
                "한 번 더, 이번엔 다른 높이에서 볼까?",
                listOf("낮게 쪼그려 앉은 자리", "길 건너 먼 곳", "눈을 감고"),
                listOf(0),
                "낮은 곳에서만 보이는 게 있었네.",
                "조금 다른 높이를 시도해 보자.",
            ),
        ),
        MysteryMechanic.LOCAL_MEMORY to listOf(
            Authored(
                InteractionKind.CHOICE,
                "이 동네 사람이 남겼을 것 같은 문장은?",
                listOf("“오늘도 빵 냄새가 좋았어요”", "“우주로 가는 길”", "“영수증 보관”"),
                listOf(0),
                "이 동네의 하루가 느껴져.",
                "그건 여기랑 잘 안 어울려.",
            ),
            Authored(
                InteractionKind.CHOICE,
                "이 장소를 좋아한 이유가 담긴 문장은?",
                listOf("“이 계단에서 첫 고양이를 만났어요”", "“바다 건너 사막”", "“주차 금지”"),
                listOf(0),
                "누군가의 소중한 기억이야.",
                "다른 문장을 읽어 보자.",
            ),
        ),
        MysteryMechanic.COMPANION_SENSE to listOf(
            Authored(
                InteractionKind.CHOICE,
                "모루가 바람이 다르다고 해. 어느 쪽일까?",
                listOf("나뭇잎이 흔들리는 쪽", "조용한 반대쪽", "뒤쪽 벽"),
                listOf(0),
                "맞아, 이쪽이야! 같이 느껴 줘서 고마워.",
                "음… 거기는 조용해. 다시 느껴 보자.",
            ),
            Authored(
                InteractionKind.CHOICE,
                "이번엔 냄새가 달라졌대. 어디서 올까?",
                listOf("꽃잎이 날아오는 쪽", "그늘진 골목", "닫힌 문"),
                listOf(0),
                "봄 냄새가 났어. 너도 맡았지?",
                "거기는 아닌가 봐. 한 번 더!",
            ),
        ),
    )

    init {
        check(MysteryMechanic.entries.all { authored[it]?.isNotEmpty() == true }) { "every mechanic needs interactions" }
    }

    /**
     * The interaction for collecting clue number [clueIndex] (1-based clue being earned). Display order
     * is shuffled deterministically from the encounter id so replays and tests are stable.
     */
    fun forStep(mechanic: MysteryMechanic, encounterId: String, clueIndex: Int): MechanicInteraction {
        val variants = authored.getValue(mechanic)
        val spec = variants[Math.floorMod(clueIndex, variants.size)]
        val order = if (spec.shuffle) shuffledOrder(spec.options.size, "$encounterId:$clueIndex") else spec.options.indices.toList()
        val displayIndexOf = order.withIndex().associate { (display, canonical) -> canonical to display }
        return MechanicInteraction(
            mechanic = mechanic,
            kind = spec.kind,
            prompt = spec.prompt,
            options = order.map { spec.options[it] },
            answer = spec.canonicalAnswer.map { displayIndexOf.getValue(it) },
            successLine = spec.success,
            retryLine = spec.retry,
        )
    }

    /** Applies one more pick to [picksSoFar]. Wrong picks reset the attempt without any penalty. */
    fun pick(interaction: MechanicInteraction, picksSoFar: List<Int>, choice: Int): InteractionProgress {
        val picks = picksSoFar + choice
        if (picks != interaction.answer.take(picks.size)) return InteractionProgress.Wrong
        return if (picks.size == interaction.answer.size) InteractionProgress.Solved else InteractionProgress.Partial(picks)
    }

    private fun shuffledOrder(size: Int, seed: String): List<Int> {
        var state = seed.fold(1125899906842597L) { acc, c -> acc * 31 + c.code }
        val list = (0 until size).toMutableList()
        for (i in list.indices.reversed()) {
            state = state * 6364136223846793005L + 1442695040888963407L
            val j = ((state ushr 33) % (i + 1)).toInt()
            val tmp = list[i]; list[i] = list[j]; list[j] = tmp
        }
        return list
    }
}
