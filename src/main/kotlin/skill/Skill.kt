package com.shadowforgedmmo.engine.skill

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.script.parseScriptId

class Skill(
    val id: String,
    val name: String,
    val description: String,
    val scriptId: String
) {
    val manaCost = 0.0 // TODO
}

fun deserializeSkill(
    id: String,
    data: JsonNode
) = Skill(
    id,
    data["name"].asText(),
    data["description"].asText(),
    parseScriptId(data["script"].asText())
)
