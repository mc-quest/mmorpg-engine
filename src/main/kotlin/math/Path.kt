package com.shadowforgedmmo.engine.math

import com.fasterxml.jackson.databind.JsonNode

class Path(val corners: List<Vector3>)

fun deserializePath(data: JsonNode) = Path(data.map(::deserializeVector3))
