package net.mcquest.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.ai.behavior.BehaviorBlueprint
import net.mcquest.engine.ai.behavior.BehaviorStatus
import net.mcquest.engine.ai.behavior.Task
import net.mcquest.engine.character.Character
import net.mcquest.engine.character.NonPlayerCharacter
import net.mcquest.engine.character.Stance
import net.mcquest.engine.math.Position
import kotlin.math.pow

class FindClosestTarget(private val radius: Double) : Task() {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        val target = character.instance.getNearbyObjects<Character>(
            character.position.toVector3(),
            radius
        )
            .filter { shouldTarget(character, it) }
            .minByOrNull { Position.sqrDistance(character.position, it.position) }
        character.target = target
        return if (target == null) BehaviorStatus.FAILURE else BehaviorStatus.SUCCESS
    }

    private fun shouldTarget(character: NonPlayerCharacter, target: Character) =
        (target !== character && target.isAlive &&
                Position.sqrDistance(
                    target.position,
                    character.position
                ) <= radius.pow(2)) &&
                character.getStance(target) === Stance.HOSTILE &&
                !target.isInvisible
}

class FindClosestTargetBlueprint(private val radius: Double) : BehaviorBlueprint() {
    override fun create() = FindClosestTarget(radius)
}

fun deserializeFindClosestTargetBlueprint(data: JsonNode) = FindClosestTargetBlueprint(
    data["radius"].asDouble()
)
