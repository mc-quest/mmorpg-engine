package net.mcquest.engine.instance

import com.fasterxml.jackson.databind.JsonNode
import net.mcquest.engine.math.Vector2Int
import net.mcquest.engine.resource.parseId
import net.mcquest.engine.runtime.Runtime
import net.mcquest.engine.world.parseWorldId
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.AnvilLoader
import net.minestom.server.world.DimensionType
import java.io.File
import java.util.*

private const val CHUNK_UNLOAD_DELAY_MILLIS = 3000

class Instance(val id: String, worldFile: File) {
    val instanceContainer = InstanceContainer(
        UUID.randomUUID(),
        DimensionType.OVERWORLD,
        AnvilLoader(worldFile.toPath())
    )
    private val chunkUnloadTimes = mutableMapOf<Vector2Int, Long>()

    fun tick(runtime: Runtime) {
        instanceContainer.chunks.forEach {
            if (it.viewers.isEmpty()) {
                val chunkCoords = Vector2Int(it.chunkX, it.chunkZ)
                if (chunkCoords !in chunkUnloadTimes) {
                    chunkUnloadTimes[chunkCoords] = runtime.timeMillis + CHUNK_UNLOAD_DELAY_MILLIS
                }
            } else {
                chunkUnloadTimes.remove(Vector2Int(it.chunkX, it.chunkZ))
            }
        }

        val toUnload = chunkUnloadTimes.filter { runtime.timeMillis >= it.value }.map { it.key }

        toUnload.forEach {
            instanceContainer.unloadChunk(it.x, it.y)
            chunkUnloadTimes.remove(it)
        }
    }
}

fun deserializeInstance(id: String, data: JsonNode, root: File) = Instance(
    id,
    root.resolve("worlds").resolve(
        parseWorldId(data["world"].asText().replace('.', '/'))
    )
)

fun parseInstanceId(id: String) = parseId(id, "instances")
