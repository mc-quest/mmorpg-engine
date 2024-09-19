package net.mcquest.engine.quest

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.character.CharacterBlueprint
import net.mcquest.engine.character.parseCharacterBlueprintId
import net.mcquest.engine.instance.Instance
import net.mcquest.engine.runtime.Runtime

abstract class QuestObjective(val goal: Int, val markers: Collection<QuestObjectiveMarker>) {
    abstract val description: String

    open fun start(runtime: Runtime, quest: Quest, objectiveIndex: Int) = Unit
}

class SlayCharacterObjective(
    goal: Int,
    markers: Collection<QuestObjectiveMarker>,
    val characterBlueprintId: String
) : QuestObjective(goal, markers) {
    override val description
        get() = "${characterBlueprint?.name} slain"
    private var characterBlueprint: CharacterBlueprint? = null

    override fun start(runtime: Runtime, quest: Quest, objectiveIndex: Int) {
        characterBlueprint = runtime.nonPlayerCharacterManager.getBlueprint(characterBlueprintId)
        runtime.questManager.registerSlayObjective(quest, objectiveIndex, characterBlueprintId)
    }
}

class CollectItemObjective(
    goal: Int,
    markers: Collection<QuestObjectiveMarker>,
    val itemBlueprintId: String
) : QuestObjective(goal, markers) {
    override val description: String
        get() = itemBlueprintId // TODO

    override fun start(runtime: Runtime, quest: Quest, objectiveIndex: Int) {
        runtime.questManager.registerItemCollectObjective(quest, objectiveIndex, itemBlueprintId)
    }
}

fun deserializeQuestObjective(
    data: JsonNode,
    instancesById: Map<String, Instance>
): QuestObjective = when (data["type"]?.asText()) {
    "slay_character" -> SlayCharacterObjective(
        data["goal"]?.asInt() ?: 1,
        deserializeQuestObjectiveMarkers(data["markers"], instancesById),
        parseCharacterBlueprintId(data["character"].asText())
    )

    "collect_item" -> CollectItemObjective(
        data["goal"]?.asInt() ?: 1,
        deserializeQuestObjectiveMarkers(data["markers"], instancesById),
        data["item"].asText()
    )

    else -> throw IllegalArgumentException("Unknown quest objective type: ${data["type"].asText()}")
}

private fun deserializeQuestObjectiveMarkers(data: JsonNode, instancesById: Map<String, Instance>) =
    data.map { deserializeQuestObjectiveMarker(it, instancesById) }
