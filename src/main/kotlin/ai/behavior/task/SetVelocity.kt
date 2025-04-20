package net.mcquest.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.ai.behavior.BehaviorBlueprint
import net.mcquest.engine.ai.behavior.BehaviorStatus
import net.mcquest.engine.ai.behavior.Task
import net.mcquest.engine.character.NonPlayerCharacter
import net.mcquest.engine.math.Vector3
import net.mcquest.engine.math.deserializeVector3

class SetVelocity(private val velocity: Vector3) : Task() {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        character.velocity = character.position.localToGlobalDirection(velocity)
        return BehaviorStatus.SUCCESS
    }
}

class SetVelocityBlueprint(
    private val velocity: Vector3
) : BehaviorBlueprint() {
    override fun create() = SetVelocity(velocity)
}

fun deserializeSetVelocityBlueprint(data: JsonNode) = SetVelocityBlueprint(
    deserializeVector3(data["velocity"])
)
