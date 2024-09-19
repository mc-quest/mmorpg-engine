package net.mcquest.engine.loot

import net.mcquest.engine.character.PlayerCharacter
import net.mcquest.engine.gameobject.GameObject
import net.mcquest.engine.gameobject.GameObjectSpawner
import net.mcquest.engine.instance.Instance
import net.mcquest.engine.math.Position
import net.mcquest.engine.runtime.Runtime

class LootChest(
    runtime: Runtime,
    spawner: LootChestSpawner
) : GameObject(runtime, spawner) {
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
    instance: Instance,
    position: Position,
    val lootTable: LootTable
) : GameObjectSpawner(instance, position) {
    override fun spawn(runtime: Runtime) = LootChest(runtime, this)
}
