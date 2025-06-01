package com.shadowforgedmmo.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.ai.behavior.Behavior
import com.shadowforgedmmo.engine.ai.behavior.BehaviorBlueprint
import com.shadowforgedmmo.engine.ai.behavior.BehaviorStatus
import com.shadowforgedmmo.engine.ai.behavior.Task
import com.shadowforgedmmo.engine.character.NonPlayerCharacter

class PlayAnimation(private val animation: String) : Task() {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        character.playAnimation(animation)
        return BehaviorStatus.SUCCESS
    }
}

class PlayAnimationBlueprint(
    private val animation: String
) : BehaviorBlueprint() {
    override fun create(): Behavior = PlayAnimation(animation)
}

fun deserializePlayAnimationBlueprint(data: JsonNode) = PlayAnimationBlueprint(
    data["animation"].asText()
)
