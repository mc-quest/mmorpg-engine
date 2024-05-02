package net.mcquest.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.ai.behavior.BehaviorBlueprint
import net.mcquest.engine.ai.behavior.BehaviorStatus
import net.mcquest.engine.ai.behavior.Task
import net.mcquest.engine.character.NonPlayerCharacter

class FaceTarget : Task() {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        val target = character.target ?: return BehaviorStatus.FAILURE
        character.lookAt(target)
        return BehaviorStatus.SUCCESS
    }
}

class FaceTargetBlueprint : BehaviorBlueprint() {
    override fun create() = FaceTarget()
}

fun deserializeFaceTargetBlueprint(
    @Suppress("UNUSED_PARAMETER") data: JsonNode
) = FaceTargetBlueprint()
