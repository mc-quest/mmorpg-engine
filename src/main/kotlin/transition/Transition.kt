package net.mcquest.engine.transition

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.character.PlayerCharacter
import net.mcquest.engine.entity.Hologram
import net.mcquest.engine.gameobject.GameObject
import net.mcquest.engine.gameobject.GameObjectSpawner
import net.mcquest.engine.instance.Instance
import net.mcquest.engine.instance.parseInstanceId
import net.mcquest.engine.math.Position
import net.mcquest.engine.math.Vector3
import net.mcquest.engine.math.deserializePosition
import net.mcquest.engine.math.deserializeVector3
import net.mcquest.engine.runtime.Runtime
import net.mcquest.engine.util.toMinestom
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType

class Transition(
    spawner: TransitionSpawner,
    instance: Instance,
    runtime: Runtime
) : GameObject(spawner, instance, runtime) {
    override val entity = Entity(EntityType.ARMOR_STAND)
    private val zoneNameHologram = Hologram()
    private val zoneLevelHologram = Hologram()

    private val toInstance
        get() = runtime.instancesById.getValue((spawner as TransitionSpawner).toInstance)

    private val toPosition
        get() = (spawner as TransitionSpawner).toPosition

    init {
        setBoundingBox(spawner.halfExtents)
    }

    override fun spawn() {
        val toZone = toInstance.zoneAt(toPosition.toVector2()) ?: error("No zone at $position")
        zoneNameHologram.text = toZone.displayName
        zoneLevelHologram.text = toZone.levelText
        val boundingBox = boundingBox
        zoneNameHologram.setInstance(
            instance.instanceContainer,
            boundingBox.center.toMinestom()
        )
        zoneLevelHologram.setInstance(
            instance.instanceContainer,
            (boundingBox.center + Vector3.DOWN * 0.4).toMinestom()
        )
    }

    override fun tick() {
        getOverlappingObjects<PlayerCharacter>().forEach {
            it.teleport(toInstance, toPosition)
        }
    }

    override fun despawn() {
        zoneNameHologram.remove()
        zoneLevelHologram.remove()
    }
}

class TransitionSpawner(
    position: Position,
    val toInstance: String,
    val toPosition: Position,
    val halfExtents: Vector3
) : GameObjectSpawner(position) {
    override fun spawn(instance: Instance, runtime: Runtime) =
        Transition(this, instance, runtime)
}

fun deserializeTransitionSpawner(data: JsonNode) = TransitionSpawner(
    deserializePosition(data["position"]),
    parseInstanceId(data["to_instance"].textValue()),
    deserializePosition(data["to_position"]),
    deserializeVector3(data["size"]) / 2.0
)
