package net.mcquest.engine.collision

import net.mcquest.engine.instance.Instance
import net.mcquest.engine.math.Vector3

open class Collider(
    instance: Instance,
    position: Vector3,
    halfExtents: Vector3,
    val onCollisionEnter: (Collider) -> Unit = {},
    val onCollisionExit: (Collider) -> Unit = {},
) {
    var instance = instance
        private set

    var position = position
        set(value) {
            val oldMin = min
            val oldMax = max
            field = value
            collisionManager?.move(oldMin, oldMax, min, max, this)
        }

    var halfExtents = halfExtents
        set(value) {
            val oldMin = min
            val oldMax = max
            field = value
            collisionManager?.move(oldMin, oldMax, min, max, this)
        }

    val min
        get() = position - Vector3(halfExtents.x, 0.0, halfExtents.z)

    val max
        get() = position + Vector3(halfExtents.x, 2.0 * halfExtents.y, halfExtents.z)

    val center
        get() = position + Vector3.UP * halfExtents.y

    var collisionManager: CollisionManager? = null

    var contacts = mutableSetOf<Collider>()

    fun setInstance(instance: Instance, position: Vector3) {
        val oldInstance = this.instance
        val oldMin = this.min
        val oldMax = this.max

        this.instance = instance
        this.position = position

        collisionManager?.move(
            oldInstance, oldMin, oldMax,
            instance, min, max,
            this
        )
    }

    fun remove() {
        collisionManager?.remove(this)
        collisionManager = null
    }
}
