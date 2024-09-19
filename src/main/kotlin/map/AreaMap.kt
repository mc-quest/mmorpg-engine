package net.mcquest.engine.map

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.math.Vector2
import net.mcquest.engine.math.deserializeVector2
import net.mcquest.engine.resource.parseId

data class AreaMap(
    val id: String,
    val origin: Vector2,
    val texture: MapTexture
)

fun deserializeMap(
    id: String,
    data: JsonNode,
    mapTexturesById: Map<String, MapTexture>
) = AreaMap(
    id,
    deserializeVector2(data["origin"]),
    mapTexturesById.getValue(parseMapTextureId(data["texture"].asText()))
)

fun parseMapId(id: String) = parseId(id, "maps")
