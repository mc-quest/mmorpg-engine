package com.shadowforgedmmo.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.ai.behavior.BehaviorBlueprint
import com.shadowforgedmmo.engine.ai.behavior.BehaviorStatus
import com.shadowforgedmmo.engine.ai.behavior.Task
import com.shadowforgedmmo.engine.character.NonPlayerCharacter

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
