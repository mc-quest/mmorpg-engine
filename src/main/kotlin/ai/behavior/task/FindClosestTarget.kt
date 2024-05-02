package net.mcquest.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.ai.behavior.BehaviorBlueprint
import net.mcquest.engine.ai.behavior.BehaviorStatus
import net.mcquest.engine.ai.behavior.Task
import net.mcquest.engine.character.Character
import net.mcquest.engine.character.NonPlayerCharacter
import net.mcquest.engine.character.Stance
import net.mcquest.engine.combat.getNearbyCharacters
import net.mcquest.engine.math.Position

class FindClosestTarget(private val radius: Double) : Task() {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        val target = getNearbyCharacters(
            character.runtime.gameObjectManager,
            character.instance,
            character.position.toVector3(),
            radius
        )
            .filter { shouldTarget(character, it) }
            .minByOrNull { Position.sqrDistance(character.position, it.position) }
        character.target = target
        return if (target == null)
            BehaviorStatus.FAILURE
        else
            BehaviorStatus.SUCCESS
    }

    private fun shouldTarget(character: NonPlayerCharacter, target: Character) =
        (target !== character && target.isAlive &&
                Position.sqrDistance(
                    target.position,
                    character.position
                ) <= radius * radius) &&
                character.getStance(target) === Stance.HOSTILE &&
                !target.isInvisible
}

class FindClosestTargetBlueprint(
    private val radius: Double
) : BehaviorBlueprint() {
    override fun create() = FindClosestTarget(radius)
}

fun deserializeFindClosestTarget(data: JsonNode) = FindClosestTargetBlueprint(
    data["radius"].asDouble()
)
