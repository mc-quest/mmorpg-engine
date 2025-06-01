package com.shadowforgedmmo.engine.loot

import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.gameobject.GameObject
import com.shadowforgedmmo.engine.gameobject.GameObjectSpawner
import com.shadowforgedmmo.engine.instance.Instance
import com.shadowforgedmmo.engine.math.Position
import com.shadowforgedmmo.engine.runtime.Runtime
import net.minestom.server.entity.EntityType
import team.unnamed.hephaestus.minestom.ModelEntity

class LootChest(
    spawner: LootChestSpawner,
    instance: Instance,
    runtime: Runtime
) : GameObject(spawner, instance, runtime) {
    override val entity = ModelEntity(EntityType.ARMOR_STAND, error(""), 1.0F)

    override fun spawn() {
        TODO()
    }

    override fun despawn() {
        TODO()
    }

    fun open(pc: PlayerCharacter) {
        val lootTable = (spawner as LootChestSpawner).lootTable
        TODO()
    }
}

class LootChestSpawner(
    position: Position,
    val lootTable: LootTable
) : GameObjectSpawner(position) {
    override fun spawn(instance: Instance, runtime: Runtime) =
        LootChest(this, instance, runtime)
}
