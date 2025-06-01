package com.shadowforgedmmo.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.ai.behavior.BehaviorBlueprint
import com.shadowforgedmmo.engine.ai.behavior.BehaviorStatus
import com.shadowforgedmmo.engine.ai.behavior.Task
import com.shadowforgedmmo.engine.character.NonPlayerCharacter
import com.shadowforgedmmo.engine.time.secondsToMillis

class Wait(private val durationMillis: Long) : Task() {
    private var startTimeMillis = 0L

    override fun start(character: NonPlayerCharacter) {
        startTimeMillis = character.runtime.timeMillis
    }

    override fun update(character: NonPlayerCharacter) =
        if (character.runtime.timeMillis - startTimeMillis >= durationMillis) {
            BehaviorStatus.SUCCESS
        } else {
            BehaviorStatus.RUNNING
        }
}

class WaitBlueprint(
    private val durationMillis: Long
) : BehaviorBlueprint() {
    override fun create() = Wait(durationMillis)
}

fun deserializeWaitBlueprint(data: JsonNode) = WaitBlueprint(
    secondsToMillis(data["duration"].asDouble())
)
