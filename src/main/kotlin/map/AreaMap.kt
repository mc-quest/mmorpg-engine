package com.shadowforgedmmo.engine.map

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.math.Vector2
import com.shadowforgedmmo.engine.math.deserializeVector2
import com.shadowforgedmmo.engine.resource.parseId

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
