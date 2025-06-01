package com.shadowforgedmmo.engine.quest

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.character.CharacterBlueprint
import com.shadowforgedmmo.engine.character.parseCharacterBlueprintId
import com.shadowforgedmmo.engine.runtime.Runtime

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
        get() = "Slay ${characterBlueprint?.name}"
    private var characterBlueprint: CharacterBlueprint? = null

    override fun start(runtime: Runtime, quest: Quest, objectiveIndex: Int) {
        if (characterBlueprintId !in runtime.characterBlueprintsById) {
            System.err.println("No such resource: characters:$characterBlueprintId")
            return
        }

        characterBlueprint = runtime.characterBlueprintsById.getValue(characterBlueprintId)
        runtime.questObjectiveManager.registerSlayObjective(quest, objectiveIndex, characterBlueprintId)
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
        runtime.questObjectiveManager.registerItemCollectObjective(quest, objectiveIndex, itemBlueprintId)
    }
}

fun deserializeQuestObjective(data: JsonNode): QuestObjective = when (data["type"]?.asText()) {
    "slay_character" -> SlayCharacterObjective(
        data["goal"]?.asInt() ?: 1,
        deserializeQuestObjectiveMarkers(data["markers"]),
        parseCharacterBlueprintId(data["character"].asText())
    )

    "collect_item" -> CollectItemObjective(
        data["goal"]?.asInt() ?: 1,
        deserializeQuestObjectiveMarkers(data["markers"]),
        data["item"].asText()
    )

    else -> throw IllegalArgumentException("Unknown quest objective type: ${data["type"].asText()}")
}

private fun deserializeQuestObjectiveMarkers(data: JsonNode) =
    data.map(::deserializeQuestObjectiveMarker)
