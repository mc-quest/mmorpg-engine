package net.mcquest.engine.zone

import net.mcquest.engine.character.PlayerCharacter
import net.mcquest.engine.datastructure.SpatialHash2
import net.mcquest.engine.instance.Instance
import net.mcquest.engine.math.Vector2

class ZoneManager(zones: Collection<Zone>) {
    private val zonesById = zones.associateBy(Zone::id)
    private val spatialHash = SpatialHash2<Zone>(512.0)

    init {
        zones.forEach {
            spatialHash.put(it.instance, it.outerBoundary.boundingBox, it)
        }
    }

    fun getZone(id: String) = zonesById.getValue(id)

    fun updateZone(pc: PlayerCharacter) {
        val position = pc.position.toVector2()
        val newZone = zoneAt(pc.instance, position)
        if (newZone != null &&
            newZone != pc.zone &&
            (!pc.zone.outerBoundary.contains(position) ||
                    newZone.type.priority > pc.zone.type.priority)
        ) {
            pc.zone = newZone
        }
    }

    fun zonesAt(instance: Instance, position: Vector2) = spatialHash.query(
        instance,
        position
    ).filter { it.boundary.contains(position) }

    fun zoneAt(instance: Instance, position: Vector2) =
        zonesAt(instance, position).maxByOrNull { it.type.priority }
}
