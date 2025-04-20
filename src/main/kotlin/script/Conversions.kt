package net.mcquest.engine.script

import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound as EngineSound
import net.mcquest.engine.math.Position as EnginePosition
import net.mcquest.engine.math.Vector3 as EngineVector3

object ScriptToEngine {
    fun vector3(v: Point) = EngineVector3(v.x, v.y, v.z)

    fun position(p: Point) =
        if (p is Position) EnginePosition(p.x, p.y, p.z, p.yaw, p.pitch)
        else EnginePosition(p.x, p.y, p.z)

    fun sound(sound: Sound) = EngineSound.sound(
        Key.key(sound.name),
        EngineSound.Source.MASTER,
        sound.volume,
        sound.pitch
    )
}

object EngineToScript {
    fun vector3(v: EngineVector3) = Vector(v.x, v.y, v.z)

    fun position(p: EnginePosition) = Position(p.x, p.y, p.z, p.yaw, p.pitch)
}
