package net.mcquest.engine.datastructure

import com.google.common.collect.HashMultimap
import com.google.common.collect.SetMultimap
import net.mcquest.engine.math.Vector3
import net.mcquest.engine.math.Vector3Int
import kotlin.math.floor

class SpatialHash3<T>(private val cellSize: Double) {
    private val grid: SetMultimap<Vector3Int, T> = HashMultimap.create()

    val values: Collection<T>
        get() = grid.values().toSet()

    fun query(position: Vector3): Collection<T> = grid[cell(position)].toSet()

    fun query(min: Vector3, max: Vector3): Collection<T> =
        cells(min, max).flatMap(grid::get).toSet()

    fun query(center: Vector3, radius: Double) = query(
        center - Vector3.ONE * radius,
        center + Vector3.ONE * radius
    )

    fun put(position: Vector3, value: T) = grid.put(cell(position), value)

    fun put(min: Vector3, max: Vector3, value: T) =
        cells(min, max).forEach { grid.put(it, value) }

    fun remove(position: Vector3, value: T) = grid.remove(cell(position), value)

    fun remove(min: Vector3, max: Vector3, value: T) =
        cells(min, max).forEach { grid.remove(it, value) }

    private fun cellCoord(coord: Double) = floor(coord / cellSize).toInt()

    private fun cell(position: Vector3) = Vector3Int(
        cellCoord(position.x),
        cellCoord(position.y),
        cellCoord(position.z)
    )

    private fun cells(min: Vector3, max: Vector3) = sequence {
        val minCell = cell(min)
        val maxCell = cell(max)

        for (x in minCell.x..maxCell.x) {
            for (y in minCell.y..maxCell.y) {
                for (z in minCell.z..maxCell.z) {
                    yield(Vector3Int(x, y, z))
                }
            }
        }
    }
}
