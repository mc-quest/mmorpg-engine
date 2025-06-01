package com.shadowforgedmmo.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.ai.behavior.BehaviorBlueprint
import com.shadowforgedmmo.engine.ai.behavior.BehaviorStatus
import com.shadowforgedmmo.engine.ai.behavior.Task
import com.shadowforgedmmo.engine.character.NonPlayerCharacter

class TargetIsToRight(private val distance: Double) : Task() {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        val target = character.target ?: return BehaviorStatus.FAILURE
        val globalOffset = target.position.toVector3() - character.position.toVector3()
        val localOffset = character.position.globalToLocalDirection(globalOffset)
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
