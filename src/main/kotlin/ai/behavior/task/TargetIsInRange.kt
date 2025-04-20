package net.mcquest.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.ai.behavior.BehaviorBlueprint
import net.mcquest.engine.ai.behavior.BehaviorStatus
import net.mcquest.engine.ai.behavior.Task
import net.mcquest.engine.character.NonPlayerCharacter
import net.mcquest.engine.math.Position

class TargetIsInRange(
    private val minDistance: Double,
    private val maxDistance: Double
) : Task() {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        val target = character.target ?: return BehaviorStatus.FAILURE
        val sqrDistanceToTarget = Position.sqrDistance(character.position, target.position)
        return if (sqrDistanceToTarget in minDistance * minDistance..maxDistance * maxDistance)
            BehaviorStatus.SUCCESS
        else
            BehaviorStatus.FAILURE
    }
}

class TargetIsInRangeBlueprint(
    private val minDistance: Double,
    private val maxDistance: Double
) : BehaviorBlueprint() {
    override fun create() = TargetIsInRange(minDistance, maxDistance)
}

fun deserializeTargetIsInRangeBlueprint(data: JsonNode) = TargetIsInRangeBlueprint(
    data["min_distance"]?.let(JsonNode::asDouble) ?: 0.0,
    data["max_distance"].asDouble()
)
