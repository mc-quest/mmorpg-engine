package com.shadowforgedmmo.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.ai.behavior.BehaviorBlueprint
import com.shadowforgedmmo.engine.ai.behavior.BehaviorStatus
import com.shadowforgedmmo.engine.ai.behavior.Task
import com.shadowforgedmmo.engine.character.NonPlayerCharacter
import com.shadowforgedmmo.engine.time.secondsToMillis

class LookAtTarget(private val durationMillis: Long) : Task() {
    private var startTimeMillis = 0L

    override fun start(character: NonPlayerCharacter) {
        startTimeMillis = character.runtime.timeMillis
    }

    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        val target = character.target ?: return BehaviorStatus.FAILURE
        character.lookAt(target)
        val time = character.runtime.timeMillis
        return if (time - startTimeMillis >= durationMillis)
            BehaviorStatus.SUCCESS
        else
            BehaviorStatus.RUNNING
    }
}

class LookAtTargetBlueprint(
    private val durationMillis: Long
) : BehaviorBlueprint() {
    override fun create() = LookAtTarget(durationMillis)
}

fun deserializeLookAtTargetBlueprint(data: JsonNode) = LookAtTargetBlueprint(
    secondsToMillis(data["duration"].asDouble())
)
