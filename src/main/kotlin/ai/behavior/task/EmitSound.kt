package com.shadowforgedmmo.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import net.kyori.adventure.sound.Sound
import com.shadowforgedmmo.engine.ai.behavior.BehaviorBlueprint
import com.shadowforgedmmo.engine.ai.behavior.BehaviorStatus
import com.shadowforgedmmo.engine.ai.behavior.Task
import com.shadowforgedmmo.engine.character.NonPlayerCharacter
import com.shadowforgedmmo.engine.math.Vector3
import com.shadowforgedmmo.engine.math.deserializeVector3
import com.shadowforgedmmo.engine.sound.deserializeSound

class EmitSound(
    private val sound: Sound,
    private val offset: Vector3
) : Task() {
    override fun update(character: NonPlayerCharacter): BehaviorStatus {
        character.emitSound(sound, offset)
        return BehaviorStatus.SUCCESS
    }
}

class EmitSoundBlueprint(
    private val sound: Sound,
    private val offset: Vector3
) : BehaviorBlueprint() {
    override fun create() = EmitSound(sound, offset)
}

fun deserializeEmitSoundBlueprint(data: JsonNode) = EmitSoundBlueprint(
    deserializeSound(data["sound"]),
    data["offset"]?.let(::deserializeVector3) ?: Vector3.ZERO
)
