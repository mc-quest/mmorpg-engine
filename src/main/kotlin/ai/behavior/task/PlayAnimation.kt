package net.mcquest.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.ai.behavior.Behavior
import net.mcquest.engine.ai.behavior.BehaviorBlueprint
import net.mcquest.engine.ai.behavior.BehaviorStatus
import net.mcquest.engine.ai.behavior.Task
import net.mcquest.engine.character.NonPlayerCharacter

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

fun deserializePlayAnimation(data: JsonNode) = PlayAnimationBlueprint(
    data["animation"].asText()
)
