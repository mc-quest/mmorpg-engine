package com.shadowforgedmmo.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.ai.behavior.BehaviorBlueprint
import com.shadowforgedmmo.engine.ai.behavior.BehaviorStatus
import com.shadowforgedmmo.engine.ai.behavior.Task
import com.shadowforgedmmo.engine.character.NonPlayerCharacter
import com.shadowforgedmmo.engine.math.Vector3
import com.shadowforgedmmo.engine.math.deserializeVector3
import com.shadowforgedmmo.engine.particle.deserializeParticle
import net.minestom.server.particle.Particle

class EmitParticle(
    private val particle: Particle,
    private val offset: Vector3
) : Task() {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        val position = character.position.toVector3() + character.position.localToGlobalDirection(offset)
        character.instance.spawnParticle(position, particle)
        return BehaviorStatus.SUCCESS
    }
}

class EmitParticleBlueprint(
    private val particle: Particle,
    private val offset: Vector3
) : BehaviorBlueprint() {
    override fun create() = EmitParticle(particle, offset)
}

fun deserializeEmitParticleBlueprint(data: JsonNode) = EmitParticleBlueprint(
    deserializeParticle(data["particle"]),
    data["offset"]?.let(::deserializeVector3) ?: Vector3.ZERO
)
