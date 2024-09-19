package net.mcquest.engine.character

import net.mcquest.engine.gameobject.GameObjectSpawner
import net.mcquest.engine.instance.Instance
import net.mcquest.engine.math.Position
import net.mcquest.engine.persistence.PlayerCharacterData
import net.mcquest.engine.runtime.Runtime
import net.minestom.server.entity.Player

class PlayerCharacterSpawner(
    instance: Instance,
    position: Position,
    val player: Player,
    val data: PlayerCharacterData
) : GameObjectSpawner(instance, position) {
    override fun spawn(runtime: Runtime): PlayerCharacter =
        PlayerCharacter(runtime, this, player, data)
}
