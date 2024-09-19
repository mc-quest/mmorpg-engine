package net.mcquest.engine.sound

import net.kyori.adventure.sound.Sound
import net.mcquest.engine.instance.Instance
import net.mcquest.engine.math.Vector3

class SoundManager {
    fun playSound(instance: Instance, position: Vector3, sound: Sound) =
        instance.instanceContainer.playSound(
            sound,
            position.x,
            position.y,
            position.z
        )
}
