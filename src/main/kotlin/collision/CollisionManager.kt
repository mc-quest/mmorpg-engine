package net.mcquest.engine.collision

import net.mcquest.engine.datastructure.SpatialHash3
import net.mcquest.engine.instance.Instance
import net.mcquest.engine.math.Vector3
import net.mcquest.engine.math.overlapBox
import net.mcquest.engine.math.boxCast

private const val CELL_SIZE = 256.0

class CollisionManager {
    private val colliders = SpatialHash3<Collider>(CELL_SIZE)
    private val movedColliders = mutableSetOf<Collider>()

    fun overlapBox(instance: Instance, min: Vector3, max: Vector3): Collection<Collider> =
        colliders.query(instance, min, max).filter { overlapBox(min, max, it.min, it.max) }

    fun overlapBoxByCenter(instance: Instance, center: Vector3, halfExtents: Vector3) =
        overlapBox(instance, center - halfExtents, center + halfExtents)

    fun raycastAll(
        instance: Instance,
        origin: Vector3,
        direction: Vector3,
        maxDistance: Double,
        predicate: (Collider) -> Boolean
    ): Collection<RaycastHit> = colliders.query(
        instance,
        origin,
        origin + direction * maxDistance
    ).filter(predicate).mapNotNull { collider ->
        boxCast(
            origin,
            direction,
            collider.min,
            collider.max,
            maxDistance
        )?.let { point -> RaycastHit(collider, point) }
    }

    fun raycast(
        instance: Instance,
        origin: Vector3,
        direction: Vector3,
        maxDistance: Double,
        predicate: (Collider) -> Boolean
    ) = raycastAll(instance, origin, direction, maxDistance, predicate)
        .minByOrNull { hit -> Vector3.sqrDistance(origin, hit.point) }

    fun add(collider: Collider) {
        collider.collisionManager = this
        val contacts = overlapBox(collider.instance, collider.min, collider.max)
        collider.contacts = contacts.toMutableSet()
        contacts.forEach { it.contacts.add(collider) }
        contacts.forEach { handleCollisionEnter(collider, it) }
        colliders.put(collider.instance, collider.min, collider.max, collider)
    }

    fun remove(collider: Collider) {
        collider.contacts.forEach { handleCollisionExit(collider, it) }
        collider.contacts.forEach { it.contacts.remove(collider) }
        collider.contacts.clear()
        colliders.remove(collider.instance, collider.min, collider.max, collider)
    }

    fun move(
        oldMin: Vector3, oldMax: Vector3,
        newMin: Vector3, newMax: Vector3,
        collider: Collider
    ) = move(
        collider.instance, oldMin, oldMax,
        collider.instance, newMin, newMax,
        collider
    )

    fun move(
        oldInstance: Instance, oldMin: Vector3, oldMax: Vector3,
        newInstance: Instance, newMin: Vector3, newMax: Vector3,
        collider: Collider
    ) {
        colliders.move(oldInstance, oldMin, oldMax, newInstance, newMin, newMax, collider)

        val oldContacts = collider.contacts.toSet()
        val newContacts = overlapBox(newInstance, newMin, newMax)
            .filterNot { it == collider }.toMutableSet()

        oldContacts.minus(newContacts).forEach { handleCollisionExit(collider, it) }
        newContacts.minus(oldContacts).forEach { handleCollisionEnter(collider, it) }

        // TODO: update contacts
        collider.contacts = newContacts
    }

    private fun handleCollisionEnter(a: Collider, b: Collider) {
        a.onCollisionEnter(b)
        b.onCollisionEnter(a)
    }

    private fun handleCollisionExit(a: Collider, b: Collider) {
        a.onCollisionExit(b)
        b.onCollisionExit(a)
    }
}

class RaycastHit(val collider: Collider, val point: Vector3)
