package com.shadowforgedmmo.engine.particle

import com.fasterxml.jackson.databind.JsonNode
import net.minestom.server.particle.Particle

fun deserializeParticle(data: JsonNode) =
    if (data.isTextual) {
        Particle.fromKey(data.asText()) ?: throw IllegalArgumentException()
    } else if (data.isObject) {
        TODO()
    } else {
        throw IllegalArgumentException()
    }
