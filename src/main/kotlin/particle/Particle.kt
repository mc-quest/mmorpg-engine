package net.mcquest.engine.particle

import com.fasterxml.jackson.databind.JsonNode
import net.minestom.server.instance.block.Block
import net.minestom.server.particle.Particle
import net.minestom.server.particle.data.BlockParticleData

fun deserializeParticle(data: JsonNode) =
    if (data.isTextual) {
        Particle.fromNamespaceId(data.asText()) ?: throw IllegalArgumentException()
    } else if (data.isObject) {
        var particle = Particle.fromNamespaceId(data["name"].asText()) ?: throw IllegalArgumentException()
        if (data.has("block")) {
            val block = Block.fromNamespaceId(data["block"].asText()) ?: throw IllegalArgumentException()
            particle = particle.withData(BlockParticleData(block))
        }
        particle
    } else {
        throw IllegalArgumentException()
    }
