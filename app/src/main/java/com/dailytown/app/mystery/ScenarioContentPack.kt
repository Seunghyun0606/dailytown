package com.dailytown.app.mystery

/**
 * Review status of a scenario copy entry. Only [AUTHORED_APPROVED] text comes from the approved
 * product scenario (EXPLORATION_EXPERIENCE_SCENARIO_V2 §6, "오래된 가로수의 메모"). Every other entry is
 * an engineering draft kept behind this boundary so the human-approved copy pack can replace it
 * without touching Compose or domain code (ROADMAP: "Approve the first authored scenario/copy pack").
 */
enum class ScenarioCopyStatus { AUTHORED_APPROVED, ENGINEERING_DRAFT }

data class ScenarioCopy(
    val title: String,
    val hint: String,
    val premise: String,
    val clueLabel: String,
    val detectMoruLine: String,
    val discoverMoruLine: String,
    val investigateMoruLine: String,
    val resolution: String,
    val resolveMoruLine: String,
)

data class ScenarioEntry(
    val mechanic: MysteryMechanic,
    val status: ScenarioCopyStatus,
    /** `{place}` is replaced with the POI display name. */
    val firstVisit: ScenarioCopy,
    val revisit: ScenarioCopy,
)

/** Versioned, validated copy pack. Lookup never fails: a missing mechanic falls back to the approved entry. */
object ScenarioContentPack {
    const val VERSION = "scenario-pack-v1"
    const val PLACE_TOKEN = "{place}"

    private val oldGinkgo = ScenarioEntry(
        mechanic = MysteryMechanic.TRACE_CHAIN,
        status = ScenarioCopyStatus.AUTHORED_APPROVED,
        firstVisit = ScenarioCopy(
            title = "오래된 가로수의 메모",
            hint = "오래된 나무 근처에서 종이 같은 게 반짝였어.",
            premise = "나무 밑에 접힌 메모 한 장이 남아 있다. 글씨는 조금 번졌지만 마지막 문장은 읽을 수 있다.",
            clueLabel = "잎자국이 남은 메모",
            detectMoruLine = "저쪽이 조금 신경 쓰이는데?",
            discoverMoruLine = "여기야. 나무 밑에 메모가 하나 있어.",
            investigateMoruLine = "잎자국이 일부러 남긴 표시처럼 보여.",
            resolution = "메모에 남은 작은 표시를 확인하고 오늘의 발견으로 기록했다.",
            resolveMoruLine = "누군가 다음 사람에게 작은 힌트를 남긴 것 같아. 우리도 기억해두자.",
        ),
        revisit = ScenarioCopy(
            title = "오래된 가로수의 메모",
            hint = "전에 걸었던 길인데, 오래된 나무 근처에서 다시 종이 같은 게 반짝였어.",
            premise = "전에 살펴본 나무 밑에 새로운 접힘 자국이 남은 메모가 보인다.",
            clueLabel = "잎자국이 남은 메모",
            detectMoruLine = "전에 봤던 곳인데, 이번엔 조금 다른 흔적 같아.",
            discoverMoruLine = "같은 장소라도 남는 이야기는 달라질 수 있나 봐.",
            investigateMoruLine = "잎자국이 전보다 또렷해. 이건 기억해두자.",
            resolution = "이번 산책에서 확인한 흔적을 기록에 덧붙였다.",
            resolveMoruLine = "우리 기억에 한 장면이 더 생겼네.",
        ),
    )

    private fun draft(
        mechanic: MysteryMechanic,
        title: String,
        hint: String,
        premise: String,
        clueLabel: String,
        detect: String,
        discover: String,
        investigate: String,
        resolution: String,
        resolve: String,
    ): ScenarioEntry {
        val first = ScenarioCopy(title, hint, premise, clueLabel, detect, discover, investigate, resolution, resolve)
        return ScenarioEntry(
            mechanic = mechanic,
            status = ScenarioCopyStatus.ENGINEERING_DRAFT,
            firstVisit = first,
            revisit = first.copy(
                hint = "전에 걸었던 $PLACE_TOKEN 근처야. $hint",
                detectMoruLine = "여기, 전에 같이 왔던 곳이지? 뭔가 달라졌어.",
                resolution = "전에 왔던 기억 위에 오늘 찾은 것을 덧붙였다.",
                resolveMoruLine = "같은 곳인데 또 새로운 이야기가 생겼네.",
            ),
        )
    }

    private val entries: Map<MysteryMechanic, ScenarioEntry> = listOf(
        oldGinkgo,
        draft(
            MysteryMechanic.SOUND_PATTERN,
            title = "${PLACE_TOKEN}의 작은 박자",
            hint = "$PLACE_TOKEN 쪽에서 일정한 박자의 소리가 들려.",
            premise = "가까이 가니 바람이 닿을 때마다 같은 박자로 울리는 것이 있다.",
            clueLabel = "세 번 울리고 쉬는 소리",
            detect = "들려? 저쪽에서 뭔가 박자를 맞추고 있어.",
            discover = "여기서 소리가 나. 같이 귀 기울여 보자.",
            investigate = "세 번 울리고 쉬어. 누가 정해둔 박자 같아.",
            resolution = "소리가 반복되는 이유를 찾아 오늘의 발견으로 기록했다.",
            resolve = "이제 이 소리를 들으면 오늘이 생각날 것 같아.",
        ),
        draft(
            MysteryMechanic.TIME_LAYER,
            title = "${PLACE_TOKEN}의 오래된 흔적",
            hint = "$PLACE_TOKEN 근처에 오래돼 보이는 표식이 하나 있어.",
            premise = "새로운 것들 사이에, 시간이 꽤 지난 표식 하나가 그대로 남아 있다.",
            clueLabel = "닳아서 흐려진 숫자",
            detect = "저기, 주변이랑 시간이 다르게 흐른 것 같은 게 있어.",
            discover = "이건 꽤 오래전부터 여기 있었나 봐.",
            investigate = "숫자가 반쯤 지워졌어. 그래도 연도는 읽을 수 있겠다.",
            resolution = "지금과 예전이 겹쳐 있는 자리를 찾아 기록했다.",
            resolve = "같은 길을 걸어도 시간은 겹겹이 남아 있구나.",
        ),
        draft(
            MysteryMechanic.SYMBOL_MATCH,
            title = "${PLACE_TOKEN}의 같은 무늬",
            hint = "$PLACE_TOKEN 주변에 같은 무늬가 자꾸 눈에 띄어.",
            premise = "비슷한 무늬가 몇 군데에 반복해서 그려져 있다.",
            clueLabel = "세 번 반복된 잎 무늬",
            detect = "아까 그 무늬, 또 본 것 같지 않아?",
            discover = "여기도 있어. 무늬를 하나씩 맞춰 보자.",
            investigate = "방향이 조금씩 달라. 어딘가를 가리키는 것 같아.",
            resolution = "흩어진 무늬를 맞춰 하나의 표시로 기록했다.",
            resolve = "누군가 길을 잃지 말라고 남겨둔 표시였나 봐.",
        ),
        draft(
            MysteryMechanic.LOST_OBJECT,
            title = "${PLACE_TOKEN}에 남겨진 물건",
            hint = "$PLACE_TOKEN 근처에 누가 뭔가 두고 간 것 같아.",
            premise = "누군가 두고 간 듯한 작은 물건 하나가 조용히 놓여 있다.",
            clueLabel = "주인을 짐작하게 하는 작은 흔적",
            detect = "저쪽에 혼자 놓인 게 있어. 가 볼래?",
            discover = "만지지 말고 먼저 잘 살펴보자.",
            investigate = "자주 이 길을 지나는 사람의 물건 같아.",
            resolution = "물건은 그 자리에 두고, 찾은 흔적만 기록했다.",
            resolve = "주인이 다시 찾으러 오면 좋겠다.",
        ),
        draft(
            MysteryMechanic.PHOTO_ANGLE,
            title = "${PLACE_TOKEN}, 다르게 보기",
            hint = "$PLACE_TOKEN 쪽 풍경이 어딘가 어긋나 보여.",
            premise = "조금만 자리를 옮기면 같은 풍경이 전혀 다르게 보인다.",
            clueLabel = "두 시점에서 달라지는 한 가지",
            detect = "저기, 보는 위치에 따라 모양이 바뀌는 것 같아.",
            discover = "한 걸음 옆으로 가서 다시 봐 볼까?",
            investigate = "여기서 보니 숨어 있던 게 보여.",
            resolution = "두 시점에서 달라지는 장면을 찾아 기록했다.",
            resolve = "익숙한 곳도 서 있는 자리만 바꾸면 새롭구나.",
        ),
        draft(
            MysteryMechanic.LOCAL_MEMORY,
            title = "${PLACE_TOKEN}에 쌓인 이야기",
            hint = "$PLACE_TOKEN 근처에 누군가의 이야기가 남아 있는 것 같아.",
            premise = "이곳을 오래 지나간 사람들이 남긴 작은 흔적들이 모여 있다.",
            clueLabel = "누군가 적어 둔 짧은 문장",
            detect = "저기, 사람들 손때가 묻은 자리가 보여.",
            discover = "여기 오래 머문 사람들이 있었나 봐.",
            investigate = "짧은 문장인데, 이 동네를 좋아했던 마음이 느껴져.",
            resolution = "이 장소에 쌓인 이야기 한 조각을 기록했다.",
            resolve = "이제 우리 이야기도 여기 조금 남았네.",
        ),
        draft(
            MysteryMechanic.COMPANION_SENSE,
            title = "모루가 느낀 $PLACE_TOKEN",
            hint = "모루가 $PLACE_TOKEN 쪽을 자꾸 돌아봐.",
            premise = "모루가 먼저 멈춰 서서 무언가를 조용히 가리킨다.",
            clueLabel = "모루만 알아챈 작은 변화",
            detect = "잠깐, 저쪽에서 뭔가 느껴져.",
            discover = "여기야. 나만 그런 게 아니면 좋겠는데.",
            investigate = "봐, 바람 방향이 여기서만 달라.",
            resolution = "모루가 알아챈 작은 변화를 함께 기록했다.",
            resolve = "같이 느껴줘서 고마워. 오늘은 조금 특별해.",
        ),
    ).associateBy { it.mechanic }

    init {
        val problems = validate()
        check(problems.isEmpty()) { "Scenario content pack $VERSION is invalid: $problems" }
    }

    fun entry(mechanic: MysteryMechanic): ScenarioEntry = entries[mechanic] ?: oldGinkgo

    fun copy(mechanic: MysteryMechanic, placeName: String, isRevisit: Boolean): ScenarioCopy {
        val entry = entry(mechanic)
        val base = if (isRevisit) entry.revisit else entry.firstVisit
        val place = placeName.ifBlank { "이 근처" }
        fun fill(text: String) = text.replace(PLACE_TOKEN, place)
        return ScenarioCopy(
            title = fill(base.title),
            hint = fill(base.hint),
            premise = fill(base.premise),
            clueLabel = fill(base.clueLabel),
            detectMoruLine = fill(base.detectMoruLine),
            discoverMoruLine = fill(base.discoverMoruLine),
            investigateMoruLine = fill(base.investigateMoruLine),
            resolution = fill(base.resolution),
            resolveMoruLine = fill(base.resolveMoruLine),
        )
    }

    /** Validation used at load time and by CI: full coverage, no blank text, only known tokens. */
    fun validate(): List<String> {
        val problems = mutableListOf<String>()
        MysteryMechanic.entries.filterNot { it in entries }.forEach { problems += "missing mechanic $it" }
        entries.values.forEach { entry ->
            listOf(entry.firstVisit, entry.revisit).forEach { copy ->
                listOf(
                    copy.title, copy.hint, copy.premise, copy.clueLabel, copy.detectMoruLine,
                    copy.discoverMoruLine, copy.investigateMoruLine, copy.resolution, copy.resolveMoruLine,
                ).forEach { text ->
                    if (text.isBlank()) problems += "${entry.mechanic}: blank text"
                    if (text.replace(PLACE_TOKEN, "").contains('{')) problems += "${entry.mechanic}: unknown token in '$text'"
                }
            }
        }
        if (entries.values.count { it.status == ScenarioCopyStatus.AUTHORED_APPROVED } != 1) {
            problems += "exactly one approved first-validation scenario expected"
        }
        return problems
    }
}
