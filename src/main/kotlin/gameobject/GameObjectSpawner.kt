package com.shadowforgedmmo.engine.gameobject

import com.shadowforgedmmo.engine.instance.Instance
import com.shadowforgedmmo.engine.math.Position
import com.shadowforgedmmo.engine.runtime.Runtime

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
