package net.mcquest.engine.loot

import net.mcquest.engine.character.PlayerCharacter
import net.mcquest.engine.gameobject.GameObject
import net.mcquest.engine.gameobject.GameObjectSpawner
import net.mcquest.engine.instance.Instance
import net.mcquest.engine.math.Position
import net.mcquest.engine.runtime.Runtime
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
