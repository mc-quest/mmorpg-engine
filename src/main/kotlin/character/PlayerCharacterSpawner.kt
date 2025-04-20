package net.mcquest.engine.character

import net.mcquest.engine.gameobject.GameObjectSpawner
import net.mcquest.engine.instance.Instance
import net.mcquest.engine.math.Position
import net.mcquest.engine.persistence.PlayerCharacterData
import net.mcquest.engine.runtime.Runtime
import net.minestom.server.entity.Player

class PlayerCharacterSpawner(
    position: Position,
    val entity: Player,
    val data: PlayerCharacterData
) : GameObjectSpawner(position) {
    override fun spawn(instance: Instance, runtime: Runtime) =
        PlayerCharacter(this, instance, runtime, entity, data)
}
