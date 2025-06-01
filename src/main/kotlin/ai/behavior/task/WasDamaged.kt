package com.shadowforgedmmo.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.ai.behavior.BehaviorBlueprint
import com.shadowforgedmmo.engine.ai.behavior.BehaviorStatus
import com.shadowforgedmmo.engine.ai.behavior.Task
import com.shadowforgedmmo.engine.character.NonPlayerCharacter
import com.shadowforgedmmo.engine.time.secondsToMillis

class WasDamaged(val durationMillis: Long) : Task() {
    override fun update(character: NonPlayerCharacter) =
//        if (timeMillis - character.lastDamagedTimeMillis <= durationMillis) BehaviorStatus.SUCCESS
//        else BehaviorStatus.FAILURE
        BehaviorStatus.FAILURE
}

class WasDamagedBlueprint(
    private val durationMillis: Long
) : BehaviorBlueprint() {
    override fun create() = WasDamaged(durationMillis)
}

fun deserializeWasDamagedBlueprint(data: JsonNode) = WasDamagedBlueprint(
    secondsToMillis(data["duration"].asDouble())
)
