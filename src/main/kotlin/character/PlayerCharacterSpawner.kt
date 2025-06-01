package com.shadowforgedmmo.engine.character

import com.shadowforgedmmo.engine.gameobject.GameObjectSpawner
import com.shadowforgedmmo.engine.instance.Instance
import com.shadowforgedmmo.engine.math.Position
import com.shadowforgedmmo.engine.persistence.PlayerCharacterData
import com.shadowforgedmmo.engine.runtime.Runtime
import net.minestom.server.entity.Player

class PlayerCharacterSpawner(
    position: Position,
    val entity: Player,
    val data: PlayerCharacterData
) : GameObjectSpawner(position) {
    override fun spawn(instance: Instance, runtime: Runtime) =
        PlayerCharacter(this, instance, runtime, entity, data)
}
