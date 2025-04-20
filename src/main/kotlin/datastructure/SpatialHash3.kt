package net.mcquest.engine.datastructure

import com.google.common.collect.HashMultimap
import com.google.common.collect.SetMultimap
import net.mcquest.engine.math.BoundingBox3
import net.mcquest.engine.math.Vector3
import net.mcquest.engine.math.Vector3Int
import kotlin.math.floor

class SpatialHash3<T>(private val cellSize: Double) {
    private val grid: SetMultimap<Vector3Int, T> = HashMultimap.create()

    val values: Collection<T>
        get() = grid.values().toSet()

    fun query(position: Vector3): Collection<T> = grid[cell(position)].toSet()

    fun query(boundingBox: BoundingBox3): Collection<T> =
        cells(boundingBox).flatMap(grid::get).toSet()

    fun put(position: Vector3, value: T) = grid.put(cell(position), value)

    fun put(boundingBox: BoundingBox3, value: T) =
        cells(boundingBox).forEach { grid.put(it, value) }

    fun remove(position: Vector3, value: T) = grid.remove(cell(position), value)

    fun remove(boundingBox: BoundingBox3, value: T) =
        cells(boundingBox).forEach { grid.remove(it, value) }

    private fun cellCoord(coord: Double) = floor(coord / cellSize).toInt()

    private fun cell(position: Vector3) = Vector3Int(
        cellCoord(position.x),
        cellCoord(position.y),
        cellCoord(position.z)
    )

    private fun cells(boundingBox: BoundingBox3) = sequence {
        val minCell = cell(boundingBox.min)
        val maxCell = cell(boundingBox.max)

        for (x in minCell.x..maxCell.x) {
            for (y in minCell.y..maxCell.y) {
                for (z in minCell.z..maxCell.z) {
                    yield(Vector3Int(x, y, z))
                }
            }
        }
    }
}
