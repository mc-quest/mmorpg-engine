package com.shadowforgedmmo.engine.sound

import net.kyori.adventure.sound.Sound
import com.shadowforgedmmo.engine.instance.Instance
import com.shadowforgedmmo.engine.math.Vector3

class SoundManager {
    fun playSound(instance: Instance, position: Vector3, sound: Sound) =
        instance.instanceContainer.playSound(
            sound,
            position.x,
            position.y,
            position.z
        )
}
