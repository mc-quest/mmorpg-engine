package net.mcquest.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.ai.behavior.BehaviorBlueprint
import net.mcquest.engine.ai.behavior.BehaviorStatus
import net.mcquest.engine.ai.behavior.Task
import net.mcquest.engine.character.NonPlayerCharacter
import net.mcquest.engine.math.Position

class TargetIsWithinDistance(private val distance: Double) : Task() {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        val target = character.target ?: return BehaviorStatus.FAILURE
        val sqrDistanceToTarget = Position.sqrDistance(
            character.position,
            target.position
        )
        return if (sqrDistanceToTarget < distance * distance)
            BehaviorStatus.SUCCESS
        else
            BehaviorStatus.FAILURE
    }
}

class TargetIsWithinDistanceBlueprint(
    private val distance: Double
) : BehaviorBlueprint() {
    override fun create() = TargetIsWithinDistance(distance)
}

fun deserializeTargetIsWithinDistance(data: JsonNode) =
    TargetIsWithinDistanceBlueprint(data["distance"].asDouble())
