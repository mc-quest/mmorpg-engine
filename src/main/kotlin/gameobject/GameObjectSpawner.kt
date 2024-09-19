package net.mcquest.engine.gameobject

import net.mcquest.engine.instance.Instance
import net.mcquest.engine.math.Position
import net.mcquest.engine.runtime.Runtime

abstract class GameObjectSpawner(
    val instance: Instance,
    val position: Position
) {
    var gameObject: GameObject? = null
    val isSpawned: Boolean
        get() = gameObject != null

    open fun start(runtime: Runtime) = Unit

    abstract fun spawn(runtime: Runtime): GameObject

    fun doSpawn(runtime: Runtime): GameObject {
        val spawnedGameObject = spawn(runtime)
        gameObject = spawnedGameObject
        return spawnedGameObject
    }
}
