package net.mcquest.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.ai.behavior.BehaviorBlueprint
import net.mcquest.engine.ai.behavior.BehaviorStatus
import net.mcquest.engine.ai.behavior.Task
import net.mcquest.engine.character.NonPlayerCharacter
import java.lang.Math.toRadians

class TargetIsToRight(private val distance: Double) : Task() {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        val target = character.target ?: return BehaviorStatus.FAILURE
        val globalOffset = target.position.toVector3() - character.position.toVector3()
        val localOffset = globalOffset.rotateAroundY(toRadians(character.position.yaw))
        return if (-localOffset.x > distance)
            BehaviorStatus.SUCCESS
        else
            BehaviorStatus.FAILURE
    }
}

class TargetIsToRightBlueprint(
    private val distance: Double
) : BehaviorBlueprint() {
    override fun create() = TargetIsToRight(distance)
}

fun deserializeTargetIsToRightBlueprint(data: JsonNode) = TargetIsToRightBlueprint(
    data["distance"]?.let(JsonNode::doubleValue) ?: 0.0
)
