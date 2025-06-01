package com.shadowforgedmmo.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import com.shadowforgedmmo.engine.ai.behavior.BehaviorBlueprint
import com.shadowforgedmmo.engine.ai.behavior.BehaviorStatus
import com.shadowforgedmmo.engine.ai.behavior.Task
import com.shadowforgedmmo.engine.character.NonPlayerCharacter
import com.shadowforgedmmo.engine.time.Cooldown
import com.shadowforgedmmo.engine.time.secondsToMillis

class CooldownReady(durationMillis: Long) : Task() {
    private val cooldown = Cooldown(durationMillis)

    override fun update(character: NonPlayerCharacter) =
        if (cooldown.hasCooldown(character.runtime.timeMillis)) {
            cooldown.set(character.runtime.timeMillis)
            BehaviorStatus.SUCCESS
        } else {
            BehaviorStatus.FAILURE
        }
}

class CooldownReadyBlueprint(
    private val durationMillis: Long
) : BehaviorBlueprint() {
    override fun create() = CooldownReady(durationMillis)
}

fun deserializeCooldownReadyBlueprint(data: JsonNode) = CooldownReadyBlueprint(
    secondsToMillis(data["duration"].asDouble())
)
