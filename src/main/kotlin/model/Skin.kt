package net.mcquest.engine.model

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.resource.parseId

class Skin(val id: String, val textures: String, val signature: String)

fun deserializeSkin(id: String, data: JsonNode) = Skin(
    id,
    data["textures"].asText(),
    data["signature"].asText()
)

fun parseSkinId(id: String) = parseId(id, "skins")
