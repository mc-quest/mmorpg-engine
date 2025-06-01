package com.shadowforgedmmo.engine.instance

import com.fasterxml.jackson.databind.JsonNode
import net.kyori.adventure.sound.Sound
import com.shadowforgedmmo.engine.character.PlayerCharacter
import com.shadowforgedmmo.engine.datastructure.SpatialHash2
import com.shadowforgedmmo.engine.datastructure.SpatialHash3
import com.shadowforgedmmo.engine.gameobject.GameObject
import com.shadowforgedmmo.engine.gameobject.GameObjectSpawner
import com.shadowforgedmmo.engine.gameobject.OBJECT_TAG
import com.shadowforgedmmo.engine.math.*
import com.shadowforgedmmo.engine.quest.Quest
import com.shadowforgedmmo.engine.resource.parseId
import com.shadowforgedmmo.engine.runtime.Runtime
import com.shadowforgedmmo.engine.util.toMinestom
import com.shadowforgedmmo.engine.world.parseWorldId
import com.shadowforgedmmo.engine.world.worldIdToWorldPath
import com.shadowforgedmmo.engine.zone.Zone
import com.shadowforgedmmo.engine.zone.parseZoneId
import net.minestom.server.MinecraftServer
import net.minestom.server.adventure.AdventurePacketConvertor
import net.minestom.server.instance.AnvilLoader
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.network.packet.server.play.ParticlePacket
import net.minestom.server.particle.Particle
import net.minestom.server.utils.PacketUtils
import net.minestom.server.world.DimensionType
import java.io.File
import java.util.*
import kotlin.math.pow
import com.shadowforgedmmo.engine.script.Instance as ScriptInstance

private const val CELL_SIZE = 64.0
private const val CHUNK_UNLOAD_DELAY_MILLIS = 3000L
private const val SPAWN_RADIUS = 75.0
private const val DESPAWN_RADIUS = 85.0

class Instance(
    val id: String,
    worldFile: File,
    zones: Collection<Zone>,
    spawners: Collection<GameObjectSpawner>
) {
    val handle = ScriptInstance(this)
    val zones = SpatialHash2<Zone>(CELL_SIZE)
    val spawners = SpatialHash3<GameObjectSpawner>(CELL_SIZE)
    val objects = SpatialHash3<GameObject>(CELL_SIZE)
    val questStarts = SpatialHash2<Quest>(CELL_SIZE)
    val questTurnIns = SpatialHash2<Quest>(CELL_SIZE)
    val instanceContainer = InstanceContainer(
        UUID.randomUUID(),
        DimensionType.OVERWORLD,
        AnvilLoader(worldFile.toPath())
    )
    private val chunkUnloadTimes = mutableMapOf<Vector2Int, Long>()

    init {
        for (zone in zones) {
            this.zones.put(zone.outerBoundary.boundingBox, zone)
        }

        for (spawner in spawners) {
            this.spawners.put(spawner.position.toVector3(), spawner)
        }
    }

    private fun zonesAt(position: Vector2) =
        zones.query(position).filter { it.boundary.contains(position) }

    fun zoneAt(position: Vector2) =
        zonesAt(position).maxByOrNull { it.type.priority }

    fun getNearbySpawners(position: Vector3, radius: Double) =
        spawners.query(BoundingBox3.from(position, Vector3.ONE * radius)).filter {
            Vector3.sqrDistance(position, it.position.toVector3()) <= radius.pow(2)
        }

    fun spawn(spawner: GameObjectSpawner, runtime: Runtime): GameObject {
        val gameObject = spawner.doSpawn(this, runtime)
        objects.put(gameObject.position.toVector3(), gameObject)
        gameObject.spawn()
        return gameObject
    }

    inline fun <reified T : GameObject> getNearbyObjects(
        position: Vector3,
        radius: Double
    ): Collection<T> = objects
        .query(BoundingBox3.from(position, Vector3.ONE * radius))
        .filterIsInstance<T>()
        .filter { Vector3.sqrDistance(position, it.position.toVector3()) <= radius.pow(2) }

    fun getNearbyPlayers(position: Vector3, radius: Double) =
        getNearbyObjects<PlayerCharacter>(position, radius).map(PlayerCharacter::entity)

    inline fun <reified T : GameObject> getObjectsInBox(box: BoundingBox3) =
        objects.query(box).filterIsInstance<T>().filter { it.boundingBox.intersects(box) }

    fun playSound(position: Vector3, sound: Sound) = PacketUtils.sendGroupedPacket(
        getNearbyPlayers(position, soundRange(sound)),
        AdventurePacketConvertor.createSoundPacket(sound, position.x, position.y, position.z)
    )

    private fun soundRange(sound: Sound) =
        16.0 * sound.volume().toDouble().coerceAtLeast(1.0)

    fun spawnParticle(
        position: Vector3,
        particle: Particle,
        longDistance: Boolean = false,
        offset: Vector3 = Vector3.ZERO,
        maxSpeed: Double = 1.0,
        count: Int = 1
    ) = PacketUtils.sendGroupedPacket(
        getNearbyPlayers(position, particleRange(longDistance)),
        ParticlePacket(
            particle,
            longDistance,
            position.x,
            position.y,
            position.z,
            offset.x.toFloat(),
            offset.y.toFloat(),
            offset.z.toFloat(),
            maxSpeed.toFloat(),
            count
        )
    )

    private fun particleRange(longDistance: Boolean) =
        if (longDistance) 512.0 else 32.0

    fun start() {
        MinecraftServer.getInstanceManager().registerInstance(instanceContainer)
        spawners.values.forEach { it.start(this) }
    }

    fun tick(runtime: Runtime) {
        manageChunks()
        manageSpawners(runtime)
        objects.values.forEach(GameObject::tick)
    }

    private fun manageChunks() {
        chunkUnloadTimes.replaceAll { _, time -> time - MinecraftServer.TICK_MS }

        val toUnload = chunkUnloadTimes.filter { it.value <= 0 }.map { it.key }

        toUnload.forEach {
            instanceContainer.unloadChunk(it.x, it.y)
            chunkUnloadTimes.remove(it)
        }

        instanceContainer.chunks.forEach {
            if (it.viewers.isEmpty()) {
                val chunkCoords = Vector2Int(it.chunkX, it.chunkZ)
                if (chunkCoords !in chunkUnloadTimes) {
                    chunkUnloadTimes[chunkCoords] = CHUNK_UNLOAD_DELAY_MILLIS
                }
            } else {
                chunkUnloadTimes.remove(Vector2Int(it.chunkX, it.chunkZ))
            }
        }
    }

    private fun manageSpawners(runtime: Runtime) {
        val pcs = objects.values.filterIsInstance<PlayerCharacter>()
        val toSpawn = getSpawnersToSpawn(pcs)
        val toNotRemove = getObjectsToNotRemove(pcs)
        val toRemove = objects.values - toNotRemove
        toRemove.forEach(GameObject::remove)
        toSpawn.forEach { spawn(it, runtime) }
    }

    private fun getSpawnersToSpawn(pcs: Collection<PlayerCharacter>) = pcs.flatMap {
        getNearbySpawners(it.position.toVector3(), SPAWN_RADIUS)
            .filterNot(GameObjectSpawner::isSpawned)
    }.toSet()

    private fun getObjectsToNotRemove(pcs: Collection<PlayerCharacter>) = pcs.flatMap {
        getNearbyObjects<GameObject>(it.position.toVector3(), DESPAWN_RADIUS)
    }.toSet()
}

fun deserializeInstance(
    id: String,
    data: JsonNode,
    root: File,
    zonesAndSpawnersByZoneId: Map<String, Pair<Zone, Collection<GameObjectSpawner>>>
): Instance {
    val zoneIds = data["zones"].map(JsonNode::asText).map(::parseZoneId)
    return Instance(
        id,
        root.resolve("worlds").resolve(
            worldIdToWorldPath(parseWorldId(data["world"].asText()))
        ),
        zoneIds.map { zonesAndSpawnersByZoneId.getValue(it).first },
        zoneIds.flatMap { zonesAndSpawnersByZoneId.getValue(it).second }
    )
}

fun parseInstanceId(id: String) = parseId(id, "instances")
