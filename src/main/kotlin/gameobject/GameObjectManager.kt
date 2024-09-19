package net.mcquest.engine.gameobject

import net.mcquest.engine.character.PlayerCharacter
import net.mcquest.engine.datastructure.SpatialHash3
import net.mcquest.engine.instance.Instance
import net.mcquest.engine.math.*
import net.mcquest.engine.runtime.Runtime

private const val CELL_SIZE = 256.0
private const val SPAWN_RADIUS = 75.0
private const val DESPAWN_RADIUS = 85.0

class GameObjectManager(spawners: Collection<GameObjectSpawner>) {
    private val spawners = SpatialHash3<GameObjectSpawner>(CELL_SIZE)
    private val objects = SpatialHash3<GameObject>(CELL_SIZE)

    init {
        spawners.forEach {
            this.spawners.put(it.instance, it.position.toVector3(), it)
        }
    }

    fun start(runtime: Runtime) {
        spawners.values.forEach { it.start(runtime) }
    }

    fun tick(runtime: Runtime) {
        tickObjects()
        spawnTick(runtime)
    }

    private fun spawnTick(runtime: Runtime) {
        val pcs = runtime.playerCharacterManager.playerCharacters
        val toSpawn = getToSpawn(pcs)
        val toNotRemove = getToNotRemove(pcs)
        val toRemove = objects.values
            .filterNot { it is PlayerCharacter }
            .filterNot(toNotRemove::contains)

        toRemove.forEach(GameObject::remove)

        toSpawn.forEach { spawner ->
            val gameObject = spawner.doSpawn(runtime)
            objects.put(
                gameObject.instance,
                gameObject.position.toVector3(),
                gameObject
            )
            gameObject.spawn()
        }
    }

    private fun getToSpawn(
        pcs: Collection<PlayerCharacter>
    ): Collection<GameObjectSpawner> = pcs.flatMap { pc ->
        spawners.query(pc.instance, pc.position.toVector3(), SPAWN_RADIUS)
            .filterNot(GameObjectSpawner::isSpawned)
            .filter { spawner ->
                Position.sqrDistance(
                    spawner.position,
                    pc.position
                ) <= SPAWN_RADIUS * SPAWN_RADIUS
            }
    }.toSet()

    private fun getToNotRemove(
        pcs: Collection<PlayerCharacter>
    ): Collection<GameObject> = pcs.flatMap { pc ->
        objects.query(pc.instance, pc.position.toVector3(), DESPAWN_RADIUS)
            .filter { gameObject ->
                Position.sqrDistance(
                    gameObject.position,
                    pc.position
                ) <= DESPAWN_RADIUS * DESPAWN_RADIUS
            }
    }.toSet()

    private fun tickObjects() =
        objects.values.toList().forEach(GameObject::tick)

    fun spawn(spawner: GameObjectSpawner, runtime: Runtime): GameObject {
        val gameObject = spawner.doSpawn(runtime)
        objects.put(
            gameObject.instance,
            gameObject.position.toVector3(),
            gameObject
        )
        gameObject.spawn()
        return gameObject
    }

    fun getNearbyObjects(
        instance: Instance,
        center: Vector3,
        radius: Double
    ): Collection<GameObject> = objects.query(instance, center, radius).filter {
        Vector3.sqrDistance(it.position.toVector3(), center) <= radius * radius
    }

    fun move(
        oldInstance: Instance,
        newInstance: Instance,
        gameObject: GameObject
    ) = move(
        oldInstance,
        gameObject.position.toVector3(),
        newInstance,
        gameObject.position.toVector3(),
        gameObject
    )

    fun move(
        oldPosition: Vector3,
        newPosition: Vector3,
        gameObject: GameObject
    ) = move(
        gameObject.instance,
        oldPosition,
        gameObject.instance,
        newPosition,
        gameObject
    )

    fun move(
        oldInstance: Instance,
        oldPosition: Vector3,
        newInstance: Instance,
        newPosition: Vector3,
        gameObject: GameObject
    ) {
        objects.move(
            oldInstance,
            oldPosition,
            newInstance,
            newPosition,
            gameObject
        )
    }

    fun remove(gameObject: GameObject) = objects.remove(
        gameObject.instance,
        gameObject.position.toVector3(),
        gameObject
    )
}
