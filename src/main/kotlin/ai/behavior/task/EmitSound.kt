package net.mcquest.engine.ai.behavior.task

import com.fasterxml.jackson.databind.JsonNode
import net.kyori.adventure.sound.Sound
import net.mcquest.engine.ai.behavior.BehaviorBlueprint
import net.mcquest.engine.ai.behavior.BehaviorStatus
import net.mcquest.engine.ai.behavior.Task
import net.mcquest.engine.character.NonPlayerCharacter
import net.mcquest.engine.math.Vector3
import net.mcquest.engine.math.deserializeVector3
import net.mcquest.engine.sound.deserializeSound

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

fun deserializeEmitSound(data: JsonNode) = EmitSoundBlueprint(
    deserializeSound(data["sound"]),
    data["offset"]?.let(::deserializeVector3) ?: Vector3.ZERO
)
