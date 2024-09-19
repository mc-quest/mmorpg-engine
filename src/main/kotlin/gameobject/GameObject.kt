package net.mcquest.engine.gameobject

import net.mcquest.engine.instance.Instance
import net.mcquest.engine.math.Position
import net.mcquest.engine.runtime.Runtime

abstract class GameObject(
    val runtime: Runtime,
    val spawner: GameObjectSpawner
) {
    var instance = spawner.instance
        set(value) {
            runtime.gameObjectManager.move(
                field,
                value,
                this
            )
            field = value
        }

    open var position = spawner.position
        set(value) {
            runtime.gameObjectManager.move(
                field.toVector3(),
                value.toVector3(),
                this
            )
            field = value
        }

    var removed = false
        private set

    open fun setInstance(instance: Instance, position: Position) {
        val oldInstance = this.instance
        val oldPosition = this.position

        runtime.gameObjectManager.move(
            oldInstance,
            oldPosition.toVector3(),
            instance,
            position.toVector3(),
            this
        )

        this.instance = instance
        this.position = position
    }

    abstract fun spawn()

    abstract fun despawn()

    open fun tick() = Unit

    fun remove() {
        despawn()
        spawner.gameObject = null
        runtime.gameObjectManager.remove(this)
        removed = true
    }
}
