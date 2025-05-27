package net.mcquest.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.ai.behavior.BehaviorBlueprint
import net.mcquest.engine.ai.behavior.BehaviorStatus
import net.mcquest.engine.ai.behavior.Task
import net.mcquest.engine.character.NonPlayerCharacter
import net.mcquest.engine.time.secondsToMillis

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
