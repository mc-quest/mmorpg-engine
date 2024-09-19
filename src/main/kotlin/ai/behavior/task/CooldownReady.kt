package net.mcquest.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.ai.behavior.BehaviorBlueprint
import net.mcquest.engine.ai.behavior.BehaviorStatus
import net.mcquest.engine.ai.behavior.Task
import net.mcquest.engine.character.NonPlayerCharacter
import net.mcquest.engine.time.Cooldown
import net.mcquest.engine.time.secondsToMillis

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
