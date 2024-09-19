package net.mcquest.engine.quest

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.instance.Instance
import net.mcquest.engine.resource.parseId
import net.mcquest.engine.runtime.Runtime

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

fun deserializeQuest(id: String, data: JsonNode, instancesById: Map<String, Instance>) = Quest(
    id,
    data["name"].asText(),
    data["level"].asInt(),
    data["prerequisites"]?.map(JsonNode::asText) ?: emptyList(),
    data["objectives"].map { deserializeQuestObjective(it, instancesById) }
)

fun parseQuestId(id: String) = parseId(id, "quests")
