package net.mcquest.engine.gameobject

import net.mcquest.engine.instance.Instance
import net.mcquest.engine.math.BoundingBox3
import net.mcquest.engine.math.Position
import net.mcquest.engine.math.Vector3
import net.mcquest.engine.runtime.Runtime
import net.mcquest.engine.util.fromMinestom
import net.mcquest.engine.util.toMinestom
import net.minestom.server.collision.BoundingBox
import net.minestom.server.entity.Entity
import net.minestom.server.tag.Tag
import team.unnamed.hephaestus.minestom.MinestomModelEngine
import team.unnamed.hephaestus.minestom.ModelEntity

val OBJECT_TAG = Tag.Transient<GameObject>("object")

abstract class GameObject(
    val spawner: GameObjectSpawner, // TODO: should this be abstract like entity? Probably
    instance: Instance,
    val runtime: Runtime
) {
    companion object {
        fun fromEntity(entity: Entity): GameObject? = entity.getTag(OBJECT_TAG)
    }

    abstract val entity: Entity

    var instance = instance
        private set

    var position = spawner.position
        private set

    var previousPosition = spawner.position
        private set

    val boundingBox
        get() = BoundingBox3(
            position.toVector3() + Vector3.fromMinestom(entity.boundingBox.relativeStart()),
            position.toVector3() + Vector3.fromMinestom(entity.boundingBox.relativeEnd()),
        )

    var removed = false
        private set

    open val removeEntityOnDespawn
        get() = true

    private var entityTeleporting = false

    fun teleport(position: Position) = teleport(instance, position)

    fun teleport(instance: Instance, position: Position) {
        this.instance.objects.remove(boundingBox, this)
        entityTeleporting = true

        val onFinish = {
            entityTeleporting = false
            this.instance = instance
            this.position = position
            instance.objects.put(boundingBox, this)
        }

        if (instance == this.instance) {
            entity.teleport(position.toMinestom()).thenRun(onFinish)
        } else {
            entity.setInstance(instance.instanceContainer, position.toMinestom()).thenRun(onFinish)
        }
    }

    inline fun <reified T : GameObject> getOverlappingObjects() =
        instance.getObjectsInBox<T>(boundingBox)

    open fun spawn() {
        val entity = entity
        entity.setTag(OBJECT_TAG, this)
        entityTeleporting = true
        entity.setInstance(instance.instanceContainer, spawner.position.toMinestom()).thenRun {
            entityTeleporting = false
        }
        if (entity is ModelEntity) {
            MinestomModelEngine.minestom().tracker().startGlobalTracking(entity)
        }
    }

    open fun despawn() {
        instance.objects.remove(boundingBox, this)
        entity.removeTag(OBJECT_TAG)
        if (removeEntityOnDespawn) entity.remove()
    }

    open fun tick() {
        previousPosition = position
        if (!entityTeleporting) {
            instance.objects.remove(boundingBox, this)
            position = Position.fromMinestom(entity.position)
            instance.objects.put(boundingBox, this)
        }
    }

    fun remove() {
        despawn()
        spawner.gameObject = null
        removed = true
    }

    fun setBoundingBox(halfExtents: Vector3) {
        val extents = halfExtents * 2.0
        entity.boundingBox = BoundingBox(extents.x, extents.y, extents.z)
    }
}
