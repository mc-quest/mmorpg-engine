package com.shadowforgedmmo.engine.quest

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.resource.parseId
import com.shadowforgedmmo.engine.runtime.Runtime

class Quest(
    val id: String,
    val name: String,
    val level: Int,
    val prerequisiteIds: Collection<String>,
    val objectives: List<QuestObjective>
) {
    fun start(runtime: Runtime) {
        for ((index, objective) in objectives.withIndex()) {
            objective.start(runtime, this, index)
        }
    }
}

fun deserializeQuest(id: String, data: JsonNode) = Quest(
    id,
    data["name"].asText(),
    data["level"].asInt(),
    data["prerequisites"]?.map(JsonNode::asText) ?: emptyList(),
    data["objectives"].map(::deserializeQuestObjective)
)

fun parseQuestId(id: String) = parseId(id, "quests")
