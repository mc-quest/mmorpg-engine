package net.mcquest.engine.transition

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.character.CharacterHitbox
import net.mcquest.engine.character.PlayerCharacter
import net.mcquest.engine.collision.Collider
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

class Transition(
    runtime: Runtime,
    spawner: TransitionSpawner
) : GameObject(runtime, spawner) {
    private val zoneNameHologram = Hologram()
    private val zoneLevelHologram = Hologram()
    private val trigger = Collider(
        instance,
        spawner.position.toVector3(),
        spawner.halfExtents,
        ::handleTriggerEnter
    )

    private val toInstance
        get() = (spawner as TransitionSpawner).toInstance

    private val toPosition
        get() = (spawner as TransitionSpawner).toPosition

    private fun handleTriggerEnter(other: Collider) {
        if (other is CharacterHitbox) {
            if (other.character is PlayerCharacter) {
                spawner as TransitionSpawner
                other.character.setInstance(spawner.toInstance, spawner.toPosition)
            }
        }
    }

    override fun spawn() {
        val toZone = runtime.zoneManager.zoneAt(
            toInstance,
            toPosition.toVector2()
        ) ?: error("No zone at $position")
        zoneNameHologram.text = toZone.displayName
        zoneLevelHologram.text = toZone.levelText
        zoneNameHologram.setInstance(
            instance.instanceContainer,
            trigger.center.toMinestom()
        ).join()
        zoneLevelHologram.setInstance(
            instance.instanceContainer,
            (trigger.center + Vector3.DOWN * 0.4).toMinestom()
        ).join()
        runtime.collisionManager.add(trigger)
    }

    override fun despawn() {
        zoneNameHologram.remove()
        zoneLevelHologram.remove()
        trigger.remove()
    }
}

class TransitionSpawner(
    instance: Instance,
    position: Position,
    val toInstance: Instance,
    val toPosition: Position,
    val halfExtents: Vector3
) : GameObjectSpawner(instance, position) {
    override fun spawn(runtime: Runtime) = Transition(runtime, this)
}

fun deserializeTransitionSpawner(
    data: JsonNode,
    instance: Instance,
    instancesById: Map<String, Instance>
) = TransitionSpawner(
    instance,
    deserializePosition(data["position"]),
    instancesById.getValue(parseInstanceId(data["to_instance"].textValue())),
    deserializePosition(data["to_position"]),
    deserializeVector3(data["size"]) / 2.0
)
