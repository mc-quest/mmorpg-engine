package net.mcquest.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.ai.behavior.BehaviorBlueprint
import net.mcquest.engine.ai.behavior.BehaviorStatus
import net.mcquest.engine.ai.behavior.Task
import net.mcquest.engine.character.NonPlayerCharacter
import net.mcquest.engine.math.Vector3
import net.mcquest.engine.math.deserializeVector3
import net.mcquest.engine.particle.deserializeParticle
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
