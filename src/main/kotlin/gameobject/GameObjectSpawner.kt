package net.mcquest.engine.gameobject

import net.mcquest.engine.instance.Instance
import net.mcquest.engine.math.Position
import net.mcquest.engine.runtime.Runtime

abstract class GameObjectSpawner(val position: Position) {
    var gameObject: GameObject? = null
    val isSpawned: Boolean
        get() = gameObject != null

    open fun start(instance: Instance) = Unit

    abstract fun spawn(instance: Instance, runtime: Runtime): GameObject

    fun doSpawn(instance: Instance, runtime: Runtime): GameObject {
        val spawnedGameObject = spawn(instance, runtime)
        gameObject = spawnedGameObject
        return spawnedGameObject
    }
}
